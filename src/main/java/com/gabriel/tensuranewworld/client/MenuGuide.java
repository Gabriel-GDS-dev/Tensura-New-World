package com.gabriel.tensuranewworld.client;

import java.util.List;
import net.minecraft.network.chat.Component;

/** Instructions describe implemented behavior, including incomplete integrations. */
final class MenuGuide {
    private MenuGuide() { }
    static List<String> lines(String tab) {
        return switch (tab) {
            case "STATUS" -> List.of("STATUS / COMO LER",
                "Esta aba consulta seu personagem; não há pontos para gastar aqui. Raça e estágio são sua evolução permanente. Transformar com Morph muda o corpo atual, não sua linhagem.",
                "EP mede poder de existência. O rank é uma faixa de EP, não um nível de experiência vanilla. Magículas mostram energia atual / capacidade máxima; habilidades podem consumir energia.",
                "Essência vem da progressão por abates. Crescimento é a quantidade de pontos livres para investir em Atributos. Almas, batalhas e capítulo são contadores separados.",
                "Use a roda do mouse dentro do conteúdo para ler os detalhes abaixo dos cartões. O objetivo do capítulo indica o próximo passo da história.",
                "HUD: VIDA inclui absorção quando houver; DEF é armadura; ATQ é dano do atributo atual. ALIMENTO e AR aparecem conforme o corpo. O HUD fica à direita para liberar o seletor do Morph.");
            case "ATTRIBUTES" -> List.of("ATRIBUTOS / PASSO A PASSO",
                "1. Derrote criaturas para obter Essência. A barra informa quanto falta para ganhar o próximo ponto; a raça influencia esse crescimento.",
                "2. Com um ponto livre, clique +1 ao lado do atributo. Cada clique consome um ponto. O botão fica indisponível sem pontos ou ao atingir 100 investimentos no atributo.",
                "Força aumenta poder físico/EP e ataque. Vitalidade aumenta vida. Agilidade aumenta movimento. Inteligência aumenta capacidade de magículas. Vontade melhora armadura.",
                "A indicação racial identifica o atributo favorecido da linhagem. Role para ver todos os atributos. Os valores exibidos são investimentos, não a vida ou o ataque final do HUD.",
                "Equipamentos, habilidades e o corpo do Morph também influenciam os valores finais. Não há botão de desfazer investimento nesta versão.");
            case "EVOLUTION" -> List.of("EVOLUÇÃO / PASSO A PASSO",
                "1. Leia sua próxima forma e todos os requisitos. [OK] significa concluído; [ ] significa pendente. Os requisitos mudam conforme a linhagem.",
                "2. Complete as condições no mundo. Quando a evolução comum estiver pronta, o botão EVOLUIR fica disponível. Clique para solicitar a evolução ao servidor.",
                "Rotas marcadas Festival da Colheita são automáticas: 1.000 almas geram a semente; 10.000 permitem o festival. O ritual dura três minutos e consome 10.000 almas ao terminar.",
                "Great Sage é inicial e gratuito. Raphael exige Lorde Demônio Verdadeiro; Ciel exige também a raça god_slime, usada pelo pack como marco de Dragão Verdadeiro. A evolução do sábio acontece automaticamente.",
                "Uma forma do Morph não substitui uma evolução de raça. Para habilidades específicas da espécie, consulte Habilidades e COMO DESBLOQUEAR. Se não houver rota registrada, a aba informa o ápice conhecido pelo New World.");
            case "FORMS" -> List.of("FORMAS / CORPO E RAÇA",
                "Com Morph instalado, esta aba informa os controles e atributos do corpo. Abra o seletor usando as teclas indicadas na aba; confirme a forma no Morph. As teclas podem ser alteradas em Opções > Controles.",
                "Sem Morph, a mimetização New World exige linhagem Slime: derrote criaturas com Predator, escolha uma forma registrada e clique ASSUMIR. ABANDONAR retorna ao corpo original.",
                "LIMPAR NW limpa somente uma forma antiga do sistema New World; para sair de uma transformação do Morph, selecione a forma original no Morph.",
                "Trocar o corpo altera atributos e necessidades físicas. A proporção da vida é preservada; transformar não cura automaticamente. Corpos que comem recuperam a alimentação anterior.",
                "Voo depende do corpo e das regras do servidor. Formas aéreas elegíveis usam dois toques em Espaço para voar e Shift para descer. Terrestres não ganham voo. Transformação e aquisição de habilidades são sistemas distintos.");
            case "SKILLS" -> List.of(
                text("guide.tensuranewworld.skills.title"),
                text("guide.tensuranewworld.skills.category"),
                text("guide.tensuranewworld.skills.details"),
                text("guide.tensuranewworld.skills.presets"),
                text("guide.tensuranewworld.skills.passives"),
                text("guide.tensuranewworld.skills.great_sage"),
                text("guide.tensuranewworld.skills.racial"));
            case "STOMACH" -> List.of(
                text("guide.tensuranewworld.stomach.title"),
                text("guide.tensuranewworld.stomach.new_world"),
                text("guide.tensuranewworld.stomach.equip"),
                text("guide.tensuranewworld.stomach.native_controls"),
                text("guide.tensuranewworld.stomach.absorption"));
            case "SUBORDINATES" -> List.of("SEGUIDORES / NOMEAÇÃO",
                "Esta aba é um registro de criaturas nomeadas pelo New World, disponível a todas as raças. Ela exibe nome, raça/identificador, EP registrado e se houve Dom do Festival da Colheita.",
                "Para registrar: fique a até oito blocos de uma criatura viva e use /tnw naming_nearest Nome. Deixe só a criatura desejada por perto: o comando escolhe a mais próxima, não a que você está olhando.",
                "Use um nome de uma palavra, com 1 a 32 caracteres. A nomeação custa 2 × EP base do alvo + 10.000 magículas. Sem energia suficiente, o comando falha e não registra a criatura.",
                "Nomear muda o nome visível e adiciona o registro. Isso não doma automaticamente animais, não torna inimigos aliados e não cria IA, botão de seguir, ordens ou teleporte.",
                "No Festival da Colheita, registros ainda sem Dom recebem marca de Dom e EP registrado multiplicado por 1,5. A aba não garante alteração real de atributos/evolução da entidade.",
                "Registre criaturas diferentes. O painel guarda registros e não oferece expulsar/renomear por botão. As listas pertencem ao personagem deste mundo.");
            case "NATION" -> List.of("NAÇÃO / FUNDAÇÃO",
                "Este registro está disponível a todas as raças. Primeiro nomeie pelo menos cinco criaturas em Seguidores. Depois use /tnw nation_found Nome da Nação.",
                "O nome pode ter espaços e deve ter de 1 a 40 caracteres. A fundação grava o nome, concede o título Leader, atualiza história/evolução e registra a própria nação com pontuação 100.",
                "Na linhagem com estágio orc, a fundação também concede King of Orcs. Isso pode satisfazer requisitos de evolução exibidos em Evolução. Outros requisitos ainda precisam ser cumpridos.",
                "Alinhamento e pontuações são dados de consulta. Nesta versão não há editor de diplomacia, territórios, proteção de blocos, cidade automática, impostos, guerra ou gestão de cidadãos nesta aba.",
                "Repetir o comando altera o nome registrado; não cria uma segunda cidade gerenciável. Construa sua cidade no mundo normalmente. Os requisitos de Seguidores/Nação não dependem de ser slime.");
            default -> List.of("Escolha uma aba para consultar suas instruções.");
        };
    }

    private static String text(String key) {
        return Component.translatable(key).getString();
    }
}
