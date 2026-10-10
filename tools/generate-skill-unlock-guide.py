"""Build the versioned guide from CFR output of the installed, external Tensura JAR.

The decompiled sources stay in tmp and are not redistributed. Only factual routes,
IDs, config references, and our own Portuguese instructions are emitted.
Usage: py tools/generate-skill-unlock-guide.py
"""
from pathlib import Path
import hashlib
import json
import re
import zipfile

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'tmp/tensura-source/io/github/manasmods/tensura'
classes = {p.stem: p for p in (SOURCE / 'ability').rglob('*.java')}
registry = {}
for p in (SOURCE / 'registry').rglob('*.java'):
    if p.parent.name not in ('skill', 'magic', 'battlewill'):
        continue
    for field, ident, cls in re.findall(r'(\w+)\s*=\s*\w+\.register\("([^"]+)",\s*(\w+)::new\)', p.read_text()):
        category = {'ResistanceSkills': 'RESISTANCE', 'IntrinsicSkills': 'INTRINSIC',
                    'CommonSkills': 'COMMON', 'ExtraSkills': 'EXTRA', 'UniqueSkills': 'UNIQUE'}.get(p.stem,
                    'MAGIC' if p.parent.name == 'magic' else 'BATTLEWILL')
        registry[p.stem + '.' + field] = ('tensura:' + ident, cls, category)

def ref(cls, field='epAcquirement'):
    path = classes[cls]
    package = re.search(r'package ([^;]+);', path.read_text()).group(1)
    return '{{' + package + '.' + cls + '#CONFIG.' + field + '}}'

def method(text, name):
    match = re.search(r'\b' + name + r'\([^)]*\)\s*\{', text)
    if not match:
        return ''
    start = match.end()
    depth = 1
    for i in range(start, len(text)):
        if text[i] == '{': depth += 1
        if text[i] == '}': depth -= 1
        if depth == 0: return text[start:i]
    return ''

rules = {}
def rule(cls, text): rules[cls] = text
for cls in ['CoercionSkill', 'GravityFieldSkill', 'GravityFlightSkill', 'StrengthSkill', 'TelepathySkill',
            'WaterCurrentControlSkill', 'AllSeeingEyeSkill', 'DangerSenseSkill', 'SteelStrengthSkill',
            'StrengthenBodySkill', 'ThoughtAccelerationSkill']:
    rule(cls, 'Aumente seu EP nativo para MAIS de ' + ref(cls) + '. Ao atualizar a capacidade de magicules, o Reincarnated registra esta skill em aprendizado.')
for cls, prerequisite in [('VoiceCannonSkill', 'Coercion'), ('WaterBladeSkill', 'Hydraulic Propulsion'),
                         ('HakiSkill', 'Coercion')]:
    rule(cls, 'Domine ' + prerequisite + ' ate o dominio maximo e alcance EP nativo MAIOR que ' + ref(cls) + '. As duas condicoes sao obrigatorias.')
for cls, spell in [('FarsightSkill', 'Clairvoyance'), ('AnalyticalAppraisalSkill', 'Analyze')]:
    rule(cls, 'Alcance EP nativo MAIOR que ' + ref(cls) + ' OU domine a magia ' + spell + '. Basta uma destas rotas.')
rule('CorrosionSkill', 'Uma destas rotas: derrote ' + ref('CorrosionSkill', 'serpentAcquirement') + ' Tempest Serpents; OU ' + ref('CorrosionSkill', 'orcAcquirement') + ' Orc Lords; OU a mesma quantidade de Orc Disasters; OU coma ' + ref('CorrosionSkill', 'rottenFleshAcquirement') + ' carnes podres. Os contadores de cada rota sao separados.')
rule('ParalysisSkill', 'Derrote pelo menos ' + ref('ParalysisSkill', 'centipedeAcquirement') + ' Evil Centipedes. Esta skill de contato e diferente do sopro Paralysing Breath.')
rule('PoisonSkill', 'Coma pelo menos ' + ref('PoisonSkill', 'spiderEyeAcquirement') + ' olhos de aranha OU derrote pelo menos ' + ref('PoisonSkill', 'spiderAcquirement') + ' Black Spiders.')
rule('RangedBarrierSkill', 'Derrote Ifrit como chefe pelo menos ' + ref('RangedBarrierSkill', 'ifritAcquirement') + ' vezes E tenha EP nativo de pelo menos ' + ref('RangedBarrierSkill') + '. Matar uma invocacao comum de Ifrit pode nao contar como chefe.')
rule('SelfRegenerationSkill', 'Derrote pelo menos ' + ref('SelfRegenerationSkill', 'slimeAcquirement') + ' slimes do Tensura, somando Slime, Metal Slime e Supermassive Slime. Slimes vanilla nao entram neste contador.')
rule('BlackFlameSkill', 'Tenha Black Lightning E Molecular Manipulation; alem disso, domine Flame Transform. Os tres requisitos sao obrigatorios.')
rule('BodyDoubleSkill', 'Tenha EP maximo nativo de pelo menos ' + ref('BodyDoubleSkill') + ' E domine Doppelganger OU tenha uma vitoria contra Ifrit chefe. A exigencia de EP vale nas duas alternativas.')
rule('ChantAnnulmentSkill', 'Domine pelo menos ' + ref('ChantAnnulmentSkill', 'magicNumber') + ' magias diferentes. Usar muitas vezes uma magia ja dominada nao aumenta a quantidade de magias dominadas.')
rule('DemonLordHakiSkill', 'Tenha Haki, alinhamento que permita tornar-se Demon Lord e EP nativo MAIOR que ' + ref('DemonLordHakiSkill') + '.')
rule('HeroHakiSkill', 'Tenha Haki, alinhamento diferente de MAJIN e EP nativo MAIOR que ' + ref('HeroHakiSkill') + '.')
for element, native in [('Earth', 'EARTH'), ('Flame', 'FLAME'), ('Lightning', 'WIND'), ('Spatial', 'SPACE'), ('Water', 'WATER'), ('Wind', 'WIND')]:
    cls = element + 'ManipulationSkill'
    rule(cls, 'Consiga nivel de espirito ' + native + ' de pelo menos 1 no armazenamento nativo de espiritos. Lightning Manipulation usa WIND nesta versao, nao um espirito separado de raio.')
for element in ['Earth', 'Flame', 'Gravity', 'Lightning', 'Sound', 'Water', 'Weather', 'Wind']:
    rule(element + 'DominationSkill', 'Domine ' + element + ' Manipulation E alcance EP nativo MAIOR que ' + ref(element + 'ManipulationSkill', 'dominationEpAcquirement') + '. A rotina de dominio tambem verifica a capacidade base de EP ao concluir o treino.')
rule('SpatialDominationSkill', 'Domine Spatial Manipulation E alcance EP nativo MAIOR que ' + ref('SpatialDominationSkill', 'dominationEpAcquirement') + '.')
rule('GodwolfSenseSkill', 'Pertenca a uma raca da tag BEASTFOLK e alcance EP nativo MAIOR que ' + ref('GodwolfSenseSkill') + '.')
rule('InfiniteRegenerationSkill', 'Tenha Ultraspeed Regeneration e alcance EP nativo MAIOR que ' + ref('InfiniteRegenerationSkill') + '.')
rule('LawManipulationSkill', 'Tenha EP nativo de pelo menos ' + ref('LawManipulationSkill') + ', domine Mana Manipulation e domine pelo menos ' + ref('LawManipulationSkill', 'magicMastered') + ' magias diferentes. Alternativa: dominar Analyst concede Law Manipulation diretamente sem custo de aquisicao.')
rule('MagicAuraSkill', 'Domine pelo menos ' + ref('MagicAuraSkill', 'magicMastered') + ' magias diferentes.')
rule('MagicSenseSkill', 'Seja de uma raca da tag SLIME OU esteja com o efeito Darkness ou Blindness quando a verificacao de aquisicao ocorrer.')
rule('MajestySkill', 'Venca pelo menos ' + ref('MajestySkill', 'raidNumber') + ' raids/invasoes registradas na estatistica RAID_WIN.')
rule('ManaManipulationSkill', 'Domine Molecular Manipulation E Magic Jamming e alcance EP nativo MAIOR que ' + ref('ManaManipulationSkill') + '.')
rule('SacredHakiSkill', 'Domine Hero Haki e desperte como True Hero. Ter somente Hero Egg nao basta.')
rule('SageSkill', 'Domine pelo menos ' + ref('SageSkill', 'abilityMastered') + ' habilidades no total, somando magias e artes de combate. Sage e uma Extra; nao e Great Sage e nao libera o addon sozinho.')
rule('UltraspeedRegenerationSkill', 'Domine Self Regeneration, derrote Charybdis chefe pelo menos uma vez e alcance EP nativo MAIOR que ' + ref('UltraspeedRegenerationSkill') + '.')
rule('DivineKiReleaseSkill', 'Pertenca a uma raca da tag nativa DIVINE.')
rule('EyeOfTruthSkill', 'Obtenha Hero Egg OU desperte como True Hero no armazenamento nativo.')
rule('PossessionSkill', 'Domine a magia Possession.')
rule('UnpredictabilitySkill', 'Desperte como True Hero; Hero Egg sozinho nao basta.')
rule('GluttonySkill', 'Tenha Starved e domine Predator. Esta combinacao registra Gluttony com 1 ponto pendente; treine o modo equipado para concluir. Gluttony nao faz parte do sorteio inicial padrao.')
rule('GodlyCraftsmanSkill', 'Domine Researcher. Ao concluir o dominio, Godly Craftsman entra em aprendizado; encantamentos guardados podem ser transferidos pela rotina nativa.')
for cls, prerequisite in [('HeavySlashArt', 'Aura Slash'), ('DeathMarchDanceArt', 'Maximum Magic Bullet'), ('MaximumMagicBulletArt', 'Magic Bullet'), ('OgreSwordCannonArt', 'Ogre Sword Guillotine'), ('HazeArt', 'Formhide')]:
    rule(cls, 'Domine a arte de combate ' + prerequisite + '. A habilidade nova entra em aprendizado.')

with zipfile.ZipFile(ROOT / 'libs/tensura-neoforge-2.0.1.3.jar') as jar:
    names = json.loads(jar.read('assets/tensura/lang/en_us.json'))
    monsters = {}
    for filename in jar.namelist():
        if filename.startswith('data/tensura/entity_existence/') and filename.endswith('.json'):
            entity = json.loads(jar.read(filename))
            for skill in entity.get('abilities', []):
                if isinstance(skill, str): monsters.setdefault(skill, []).append(entity.get('type', filename))

racial = {}
for p in (SOURCE / 'race').rglob('*.java'):
    body = method(p.read_text(), 'getIntrinsicSkills')
    for reference in re.findall(r'((?:Common|Extra|Intrinsic|Resistance|Unique)Skills\.\w+)\.get\(', body):
        if reference in registry:
            racial.setdefault(registry[reference][0], []).append(p.stem.removesuffix('Race'))

mastery = {}
for p in (SOURCE / 'ability').rglob('*.java'):
    body = method(p.read_text(), 'onSkillMastered')
    if body and 'learnSkill' in body and not any(t in body for t in ['hasSkill', 'getBaseMaxEP', 'getRace', 'isTrueHero']):
        for reference in re.findall(r'(\w+(?:Skills|Magics|Arts)\.\w+)\.get\(', body):
            if reference in registry: mastery.setdefault(registry[reference][0], []).append(p.stem)

pool_text = (SOURCE / 'config/ReincarnationConfig.java').read_text()
pool_match = re.search(r'startingSkills\s*=\s*List\.of\((.*?)\);', pool_text, re.S)
pool = set(re.findall(r'"(tensura:[^"]+)"', pool_match.group(1))) if pool_match else set()
entries = {}
for reference, (ident, cls, category) in registry.items():
    steps = []
    if cls in rules: steps.append('ROTA NATURAL: ' + rules[cls])
    if ident in pool:
        steps.append('SORTEIO: esta Unique participa da lista inicial padrao de reencarnacao. Conclua a escolha de raca no menu nativo; com a configuracao padrao, recebe 1 Unique aleatoria. Nao e uma escolha garantida desta skill. O Skill Reset Scroll refaz o sorteio, mas pode remover racas/habilidades e aplicar as penalidades de reset; nao use para preservar sua ficha atual. Listas e restricao trulyUnique podem alterar as chances.')
    if category == 'RESISTANCE':
        if ident.endswith('_nullification'):
            steps.append('DESPERTAR: a rotina nativa RaceHelper.awakening transforma resistencias permanentes ja aprendidas (dominio >= 0) na anulacao correspondente, quando existe essa correspondencia. Conclua o despertar nativo de True Demon Lord/True Hero com a resistencia anterior aprendida. Subir somente seu dominio nao dispara essa rotina. Outra rota e o kit racial ou a absorcao de um mob que ja possua esta anulacao. A gamerule disableNullification=true impede seus efeitos.')
        else:
            steps.append('TREINO: se constar na lista learnableResistances do servidor, o Reincarnated registra esta resistencia com dominio negativo ao entrar no mundo. Receba dano ou o efeito correspondente ao nome da resistencia; a ficha mostra os pontos faltantes. O progresso depende da quantidade de dano, dos limiares da resistencia e do ganho de aprendizado nativo, nao de uma quantidade fixa de golpes. Pare quando o dominio chegar a zero. Depois ligue a passiva na ficha.')
    if category == 'MAGIC':
        steps.append('TOMO: obtenha um Magic Tome cujo tooltip identifique exatamente esta magia. Segure usar com ele por pelo menos 10 ticks (0,5 segundo) e solte para registrar a magia em aprendizado. Um tomo sem magia definida sorteia magia Aspectual; nao garante esta. O item e consumido e recebe 200 ticks de recarga. Depois equipe e treine os modos.')
    if category == 'BATTLEWILL':
        steps.append('MANUAL: obtenha um Battlewill Manual cujo tooltip identifique esta arte. Segure usar por pelo menos 10 ticks (0,5 segundo) e solte. A arte entra em aprendizado; equipe em um slot e treine no mundo. Um manual sem arte definida sorteia uma arte e nao garante esta.')
    prey = monsters.get(ident, [])
    if category in ('INTRINSIC', 'COMMON', 'EXTRA', 'RESISTANCE') and prey:
        steps.append('ABSORCAO NEW WORLD: como slime com Predator/Gluttony/Beelzebuth, derrote um destes mobs: ' + ', '.join(sorted(set(prey))) + '. A criatura precisa ter esta habilidade permanente e aprendida (dominio >= 0). Basta um abate elegivel; a aquisicao e gratuita e permanente. Invocacoes ou variantes sem a skill nao servem.')
    if racial.get(ident):
        steps.append('KIT RACIAL: esta skill e referenciada nos kits de ' + ', '.join(sorted(set(racial[ident]))) + '. A concessao depende de atingir a raca/variante e das condicoes do kit. Ser apenas slime nao concede automaticamente kits de outras racas. Consulte a evolucao nativa correspondente antes de mudar de raca.')
    for source_cls in mastery.get(ident, []):
        steps.append('DOMINIO: conclua o dominio da habilidade independente ' + source_cls.removesuffix('Skill').removesuffix('Magic').removesuffix('Art') + '; sua rotina nativa concede esta habilidade. Uma subhabilidade embutida em outra skill nao aciona esta recompensa.')
    if not steps:
        steps.append('Esta habilidade nao possui uma condicao automatica de EP/abates nem um sorteio inicial padrao identificados para esta versao. Nao existe um numero de EP que garanta sua aquisicao. Use uma concessao nativa especifica, um alvo que possa ser saqueado com as regras nativas ou a rota administrativa abaixo; o New World nao inventa um desbloqueio por EP.')
    steps.append('TESTE/ADMINISTRADOR: com comandos habilitados e permissao de operador, use /tensura edit ability grant @s ' + ident + '. Este comando nativo concede a habilidade aprendida e permanente sem cobrar a capacidade de magicules. Nao use /tnw debug grant_skill: ele so cria um registro New World. Isto e uma alternativa administrativa, nao uma rota de sobrevivencia.')
    entries[ident] = {'name': names.get('tensura.skill.' + ident.split(':')[1], names.get('tensura.magic.' + ident.split(':')[1], ident)), 'category': category, 'steps': steps}

entries['tensura:great_sage']['steps'].insert(0, 'GREAT SAGE EXATO: o registro tensuranewworld:great_sage nao concede esta skill. No seu personagem atual, a forma direta para teste e /tensura edit ability grant @s tensura:great_sage. Sem comandos, a rota inicial padrao e o sorteio da reencarnacao; nao ha desbloqueio garantido por EP nem por matar centopeias. Em um mundo NOVO, habilitar /gamerule rimuruMode true ANTES da primeira reencarnacao concede Slime + Predator + Great Sage. Ativar a regra depois que voce ja tem raca nao aplica esse conjunto retroativamente. O conjunto Rimuru nao cobra o custo de compra da skill.')
entries['tensura:great_sage']['steps'].append('USAR: confirme tensura:great_sage em R > Habilidades > Unicas. Ligue a passiva para os bonus de pensamento/aprendizado. Equipe um modo nativo para usar avaliacao, analise ou refinamento. Com o addon Great Sage instalado, G abre sua interface e V faz analise conforme os controles configurados. Instalar o addon sozinho nao concede a skill nativa.')
entries['tensura:great_sage']['steps'] = ['PADRAO NEW WORLD: todos os jogadores recebem Great Sage nativo automaticamente ao entrar, incluindo personagens existentes, de qualquer raca. Nao exige comandos, EP, sorteio ou capacidade de magicules. A habilidade e permanente e ja aprendida. Quem ja possui Great Sage conserva seu dominio e suas configuracoes. A evolucao nativa para Raphael nao e revertida.', 'USAR: em R > Habilidades > Unicas, selecione Great Sage. Ligue a passiva para os bonus e equipe um modo nativo se desejar. Com o addon instalado, G abre a interface e V analisa conforme Controles. A aquisicao gratuita nao elimina os custos nativos de uso dos modos.']
entries['tensura:sage']['steps'].append('ATENCAO: Sage (Extra) e Great Sage (Unique) sao habilidades diferentes. Dominar varias magias para obter Sage nao concede automaticamente Great Sage.')
entries['greatsage:raphael'] = {'name': 'Raphael', 'category': 'ULTIMATE', 'steps': ['ADDON 0.0.6.1: a evolucao de sobrevivencia esta desativada: CapstoneGate.available retorna false. Mesmo com Great Sage e Degenerate dominadas, True Hero/True Demon Lord, 500000 EP, 50000 MP maximo e todos os nos, esta versao nao libera o capstone.', 'TESTE/ADMINISTRADOR: /tensura edit ability grant @s greatsage:raphael. Exige comandos e permissao de operador. O registro New World sozinho nao concede a habilidade nativa.']}
entries['greatsage:venuzdonoa'] = {'name': 'Venuzdonoa', 'category': 'UNIQUE', 'steps': ['ADDON 0.0.6.1: nao foi identificada uma rota automatica de sobrevivencia para esta habilidade. Nao ha um limiar de EP confirmado.', 'TESTE/ADMINISTRADOR: /tensura edit ability grant @s greatsage:venuzdonoa. Exige comandos e permissao de operador.']}
entries['tensuranewworld:raphael'] = {'name': 'Raphael, Lord of Wisdom', 'category': 'ULTIMATE', 'steps': ['EVOLUCAO NEW WORLD: comece com Great Sage gratuito. Obtenha a Semente de Lorde Demonio com 1000 almas, acumule 10000 almas e conclua o Festival da Colheita (3 minutos). Ao confirmar o estado nativo de Verdadeiro Lorde Demonio, Great Sage e substituido automaticamente por Raphael. Personagens ja despertos recebem a evolucao ao entrar. Nao basta ser Demon Lord Seed ou ter EP alto. Nesta adaptacao facilitada, Degenerate nao e exigida nem sacrificada.', 'USAR: os modos nativos de avaliacao, analise e refinamento continuam disponiveis. O quarto modo, Otimizacao, reduz recargas de outras skills aprendidas em 25%, por 5000 magicules, com recarga propria de 60 segundos. Nao reduz a recarga de Raphael/Ciel. Esta acao e uma adaptacao de gameplay.', 'O estagio preserva dominio, armazenamento e os slots equipados da habilidade anterior. As outras habilidades Supremas nao sao concedidas automaticamente.']}
entries['tensuranewworld:ciel'] = {'name': 'Ciel', 'category': 'MANAS', 'steps': ['EVOLUCAO NEW WORLD: seja Verdadeiro Lorde Demonio e alcance a raca nativa tensura:god_slime (Ultimate Slime). Na arvore atual: Named Slime exige 100000 EP e receber um nome; Demon Slime exige 500000 EP e o despertar; Ultimate Slime exige 2500000 EP e o estado de Verdadeiro Lorde Demonio. Conclua a evolucao racial disponivel no painel. Ao confirmar god_slime, Raphael recebe automaticamente o nome Ciel e evolui para Manas.', 'A raca god_slime representa o marco de Dragao Verdadeiro de Rimuru nesta adaptacao; True Dragonewt e Divine Dragon NAO contam. Na historia, a nomeacao e o evento decisivo de Ciel; nao e uma evolucao causada simplesmente por mudar de raca.', 'USAR: mantem os modos de Raphael. Otimizacao melhora para 50% de reducao de recargas de outras skills aprendidas, custa 15000 magicules e tem 60 segundos de recarga propria. Nao reduz recargas de Raphael/Ciel. Nao concede Azathoth, criacao irrestrita de habilidades ou energia infinita.', 'Ciel e o ultimo estagio desta linha. Azathoth e outra habilidade, nao uma evolucao seguinte de Ciel.']}
out = ROOT / 'src/main/resources/data/tensuranewworld/skill_unlock_guide.json'
out.parent.mkdir(parents=True, exist_ok=True)
out.write_text(json.dumps({'version': 'tensura-2.0.1.3', 'source_sha256': hashlib.sha256((ROOT / 'libs/tensura-neoforge-2.0.1.3.jar').read_bytes()).hexdigest(), 'skills': dict(sorted(entries.items()))}, ensure_ascii=False, indent=2), encoding='utf-8')
print(f'Wrote {len(entries)} guides; {len(pool)} starting unique IDs; {len(rules)} exact automatic rules.')
