package com.gabriel.tensuranewworld.integration;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import net.minecraft.world.entity.LivingEntity;

/** Factual versioned routes; configuration numbers are resolved on the server. */
public final class SkillUnlockGuide {
    private static final Pattern CONFIG = Pattern.compile("\\{\\{([^#}]+)#([^}]+)}}");
    private static final JsonObject GUIDES = load();

    private SkillUnlockGuide() { }

    private static JsonObject load() {
        try (var stream = SkillUnlockGuide.class.getResourceAsStream("/data/tensuranewworld/skill_unlock_guide.json")) {
            if (stream == null) throw new IllegalStateException("Missing skill unlock guide");
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonObject("skills");
        } catch (java.io.IOException error) {
            throw new IllegalStateException("Cannot read skill unlock guide", error);
        }
    }

    public static boolean contains(String id) { return GUIDES.has(nativeId(id)); }
    public static int size() { return GUIDES.size(); }
    private static String nativeId(String id) {
        if (GUIDES.has(id)) return id;
        if (id.equals("tensuranewworld:raphael")) return "greatsage:raphael";
        return id.startsWith("tensuranewworld:") ? "tensura:" + id.substring(id.indexOf(':') + 1) : id;
    }

    public static String text(String id, Object skill, LivingEntity entity) {
        var guide = GUIDES.getAsJsonObject(nativeId(id));
        if (guide == null) {
            return "DESBLOQUEIO: nao existe um procedimento confirmado neste guia para este registro. "
                    + "EP e a marca de progressao New World nao concedem automaticamente a habilidade nativa. "
                    + "Consulte a concessao especifica do mod que registrou esta habilidade.";
        }
        StringBuilder text = new StringBuilder("COMO DESBLOQUEAR (Reincarnated 2.0.1.3)\n");
        int index = 1;
        for (var step : guide.getAsJsonArray("steps")) {
            text.append(index++).append(". ").append(resolve(step.getAsString())).append('\n');
        }
        if (skill != null && entity != null) {
            Object points = ReincarnatedSkills.call(skill, "getAcquirementMastery", entity);
            Object cost = ReincarnatedSkills.call(skill, "getDefaultAcquiringMagiculeCost");
            if (points instanceof Number value && value.doubleValue() < 0) {
                text.append("APRENDIZADO INICIAL: ").append(amount(-value.doubleValue()))
                        .append(" pontos quando adquirida como skill em aprendizado. Equipe e tente usar; passivas/resistencias seguem seus gatilhos especificos. Absorcao New World e grant administrativo concedem a skill aprendida.\n");
            }
            if (nativeId(id).equals("tensura:great_sage")) {
                text.append("CUSTO DE AQUISICAO NEW WORLD: 0. Concessao automatica sem reduzir a capacidade maxima. Custos de uso dos modos continuam nativos.\n");
            } else if (cost instanceof Number value) {
                double multiplier = 1;
                try {
                    Object key = field(Class.forName("io.github.manasmods.tensura.world.TensuraGameRules"), null, "MP_SKILL_COST");
                    Object percent = ReincarnatedSkills.call(entity.level().getGameRules(), "getInt", key);
                    if (percent instanceof Number number) multiplier = number.doubleValue() / 100;
                } catch (ReflectiveOperationException | RuntimeException ignored) { }
                text.append("CUSTO DE AQUISICAO NATIVA: ").append(amount(value.doubleValue() * multiplier))
                        .append(" magicules de CAPACIDADE, conforme a gamerule mpSkillCost. Quando cobrado, exige capacidade MAIOR que o custo e reduz a capacidade maxima permanentemente. Ter so energia atual nao basta. Kit racial aplicavel, conjunto Rimuru, absorcao New World e comando grant podem dispensar esse custo. O custo de USO e separado.\n");
            }
        }
        return text.toString().stripTrailing();
    }

    private static String resolve(String value) {
        var matcher = CONFIG.matcher(value);
        return matcher.replaceAll(match -> {
            try {
                Class<?> type = Class.forName(match.group(1));
                Object current = null;
                for (String name : match.group(2).split("\\.")) {
                    current = field(type, current, name);
                    if (current != null) type = current.getClass();
                }
                return current instanceof Number number ? amount(number.doubleValue()) : "valor da configuracao do servidor";
            } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
                return "valor da configuracao do servidor";
            }
        });
    }

    private static Object field(Class<?> type, Object target, String name) throws ReflectiveOperationException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException ignored) { }
        }
        throw new NoSuchFieldException(name);
    }

    private static String amount(double number) {
        return String.format(java.util.Locale.ROOT, number == Math.floor(number) ? "%.0f" : "%.1f", number);
    }
}
