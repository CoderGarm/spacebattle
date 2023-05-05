package de.yuga.spacebattle.misc.fandom.spacecraft;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import de.yuga.spacebattle.TestUtils;
import de.yuga.spacebattle.backend.enums.EShipClassType;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.misc.fandom.EWikiCategories;
import de.yuga.spacebattle.misc.fandom.EWikiConfig;
import de.yuga.spacebattle.misc.fandom.FandomWikiQueryTest;
import de.yuga.spacebattle.misc.fandom.spacecraft.dto.FieldName;
import de.yuga.spacebattle.misc.fandom.spacecraft.dto.Weaponry;
import de.yuga.spacebattle.misc.fandom.spacecraft.dto.WikiShipClass;
import de.yuga.spacebattle.misc.fandom.spacecraft.dto.classes.ClassesByAffiliation;
import de.yuga.spacebattle.misc.fandom.spacecraft.dto.classes.ClassesByIntroductionDate;
import de.yuga.spacebattle.misc.fandom.spacecraft.dto.classes.ClassesByIntroductionDateAndType;
import de.yuga.spacebattle.misc.fandom.spacecraft.dto.classes.WeaponsPerAlignmentPerShipClassTypePerAffiliation;
import org.jfree.chart.ui.UIUtils;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Year;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class WikiShipClassEvaluationTest {

    public static final String S_1_PD = "[\\s]?[0-9]{4}[\\s]?(PD)?";
    private final Map<FieldName, List<WikiShipClass>> errors = new HashMap<>();

    @Test
    void evaluateShipClasses() {

        final List<WikiShipClass> wikiShipClasses = TestUtils.readShipClasses(FandomWikiQueryTest.DIR,
                EWikiCategories.SHIP_CLASSES.getFolder(EWikiConfig.DE)).stream().collect(Collectors.toList());

        assertFalse(wikiShipClasses.isEmpty());
        //createTimeChart(wikiShipClasses);

        final Map<EShipClassType, List<WikiShipClass>> byType = wikiShipClasses.stream().collect(Collectors.groupingBy(WikiShipClass::getShipClassType, Collectors.mapping(Function.identity(), Collectors.toList())));

        //printWeaponLoadout(byType);
        printTonnagePerShipClassType(byType);

        /*
        final List<WeaponsPerAlignmentPerShipClassTypePerAffiliation> result = getWeaponsCountPerShipClassType(byType);
        printWeaponsCountPerShipClassType(result);
        */
    }

    private static void printTonnagePerShipClassType(final Map<EShipClassType, List<WikiShipClass>> byType) {

        byType.forEach((shipClassType, list) -> {
            System.out.println(shipClassType);

            final List<WikiShipClass> byTonnage = list.stream().sorted((Comparator.comparingInt(o -> o.getClassPhysics().getMasse()))).collect(Collectors.toList());

            System.out.println("\t min:" + byTonnage.get(0).getClassPhysics().getMasse() + " t max: " + byTonnage.get(byTonnage.size() - 1).getClassPhysics().getMasse());
        });
    }

    private static void printWeaponsCountPerShipClassType(final List<WeaponsPerAlignmentPerShipClassTypePerAffiliation> result) {
        final int length = result.stream().sorted(Comparator.comparingInt(o -> o.getAffiliation().length())).map(w -> w.getAffiliation().length()).reduce((o1, o2) -> o2).orElse(0);
        final String placeholder = Strings.repeat(" ", length);
        final Map<EShipClassType, List<WeaponsPerAlignmentPerShipClassTypePerAffiliation>> typeListMap = result.stream().collect(Collectors.groupingBy(WeaponsPerAlignmentPerShipClassTypePerAffiliation::getShipClassType, Collectors.mapping(Function.identity(), Collectors.toList())));
        typeListMap.forEach((shipClassType, list) -> {
            System.out.println(shipClassType);
            list.forEach(dto -> {
                final String collect = dto.getMap().entrySet().stream().sorted(Comparator.comparingInt(o -> o.getKey().ordinal())).map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining("\t"));
                final String ph = placeholder.substring(0, length - dto.getAffiliation().length());
                System.out.println("\t" + dto.getAffiliation() + ":" + ph + "\t" + collect);
            });
        });
    }

    private static List<WeaponsPerAlignmentPerShipClassTypePerAffiliation> getWeaponsCountPerShipClassType(final Map<EShipClassType, List<WikiShipClass>> byType) {

        final List<WeaponsPerAlignmentPerShipClassTypePerAffiliation> result = new ArrayList<>();
        byType.forEach((type, shipClasses) -> {
            final Map<String, List<WikiShipClass>> byAffiliation = shipClasses.stream().collect(Collectors.groupingBy(WikiShipClass::getAffiliation, Collectors.mapping(Function.identity(), Collectors.toList())));
            byAffiliation.forEach((affiliation, wikiShipClasses1) -> {

                final Map<EShipClassType, List<WikiShipClass>> types = wikiShipClasses1.stream()
                        .collect(Collectors.groupingBy(WikiShipClass::getShipClassType,
                                Collectors.mapping(Function.identity(), Collectors.toList())));

                types.forEach((type1, wikiShipClasses) -> {
                    final WeaponsPerAlignmentPerShipClassTypePerAffiliation dto = new WeaponsPerAlignmentPerShipClassTypePerAffiliation(affiliation, type1);
                    wikiShipClasses.forEach(dto::add);
                    result.add(dto);
                });
            });
        });
        return result;
    }


    private void createTimeChart(final List<WikiShipClass> wikiShipClasses) {
        final List<ClassesByAffiliation> result = getClassesByAffiliations(wikiShipClasses);
        assertFalse(result.isEmpty());
        logErrors();
        final Map<String, Map<String, Map<String, List<WikiShipClass>>>> byAffiliationAndTypeAndDate = sortByAffiliation(result);
        byAffiliationAndTypeAndDate.forEach((affiliation, byTypeAndIntroductionDate) -> {

            final TimeSeriesCollection dataset = new TimeSeriesCollection();
            byTypeAndIntroductionDate.forEach((type, byIntroductionDates) -> {
                final TimeSeries timeSeries = new TimeSeries(type);

                byIntroductionDates.forEach((introductionDate, shipClasses) -> {
                    final int year = Integer.parseInt(introductionDate.replaceAll(" ", "").replaceAll("PD", ""));

                    final List<Integer> masses = shipClasses.stream()
                            .map(WikiShipClassEvaluationTest::sanitizeTonnage)
                            .map(Integer::parseInt)
                            .collect(Collectors.toList());

                    final Integer sum = masses.stream().reduce(0, Integer::sum) / masses.size();
                    timeSeries.add(new Year(year), sum);
                });
                dataset.addSeries(timeSeries);
            });

            final XYLineTimeChart demo = new XYLineTimeChart(affiliation, dataset);
            demo.pack();
            UIUtils.centerFrameOnScreen(demo);
            demo.setVisible(true);
        });

        runIndefinite();
    }

    private static void printWeaponLoadout(final Map<EShipClassType, List<WikiShipClass>> byType) {
        byType.forEach((type, shipClasses) -> {

            final Map<String, List<WikiShipClass>> byAffiliation = shipClasses.stream().collect(Collectors.groupingBy(WikiShipClass::getAffiliation, Collectors.mapping(Function.identity(), Collectors.toList())));

            byAffiliation.forEach((affiliation, wikiShipClasses1) -> {
                System.out.println(affiliation);
                wikiShipClasses1.forEach(wikiShipClass -> {
                    final String name = wikiShipClass.getName();
                    System.out.println("\t" + name + ", " + wikiShipClass.getShipClassType() + " with " + wikiShipClass.getClassPhysics().getMasse() + " t from " + wikiShipClass.getIntroductionDate());

                    final Weaponry weaponry = wikiShipClass.getWeaponry();
                    final Map<String, Integer> weaponSet = weaponry.getWeaponSet();
                    final String weaponSetString = weaponSet.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining(","));
                    System.out.println("\t\t" + weaponSetString);

                    final Map<EWeaponAlignment, Map<String, Integer>> alignmentSet = weaponry.getAlignmentSet();
                    alignmentSet.forEach((eAlignmentType, amountByWeaponType) -> {
                        final String collect = amountByWeaponType.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining(","));
                        System.out.println("\t\t\t" + eAlignmentType + ": " + collect);
                    });
                });
            });
        });
    }

    @Nonnull
    private static String sanitizeTonnage(final WikiShipClass s) {
        return s.getRawValue(FieldName.Masse).replaceAll(" ", "").replaceAll("t", "").replaceAll("Tonnen", "").replaceAll("\\.", "").replaceAll(",", "")
                ;
    }

    private static Map<String, Map<String, Map<String, List<WikiShipClass>>>> sortByAffiliation(@Nonnull final List<ClassesByAffiliation> result) {
        Preconditions.checkNotNull(result, "result must not be empty");

        final Map<String, List<ClassesByIntroductionDateAndType>> classesByAffiliation = result.stream()
                .collect(Collectors.toMap(ClassesByAffiliation::getAffiliation, ClassesByAffiliation::getByDateAndType));

        final Map<String, Map<String, Map<String, List<WikiShipClass>>>> byAffiliation = new HashMap<>();
        classesByAffiliation.forEach((affiliation, byIntroductionDateAndTypes) -> {
            final Map<String, Map<String, List<WikiShipClass>>> byType = byAffiliation.getOrDefault(affiliation, new HashMap<>());

            final Map<String, List<ClassesByIntroductionDate>> byTypeAndIntroductionDate = byIntroductionDateAndTypes.stream()
                    .collect(Collectors.toMap(ClassesByIntroductionDateAndType::getShipType, ClassesByIntroductionDateAndType::getByIntroductionDate));

            byTypeAndIntroductionDate.forEach((type, byIntroductionDates) -> {
                final Map<String, List<WikiShipClass>> byIntroDate = byIntroductionDates.stream()
                        .collect(Collectors.toMap(ClassesByIntroductionDate::getIntroductionDate, ClassesByIntroductionDate::getClasses));
                byType.put(type, byIntroDate);
            });
            byAffiliation.put(affiliation, byType);
        });
        return byAffiliation;
    }

    private static void runIndefinite() {
        while (true) {

        }
    }

    @Nonnull
    private List<ClassesByAffiliation> getClassesByAffiliations(final List<WikiShipClass> wikiShipClasses) {
        final List<ClassesByAffiliation> result = new ArrayList<>();
        final Map<String, List<WikiShipClass>> byAffiliation = groupByAffiliation(wikiShipClasses);
        byAffiliation.forEach((affiliation, shipClasses) -> {
            final List<ClassesByIntroductionDateAndType> collector = new ArrayList<>();
            final Map<String, List<WikiShipClass>> byType = groupByShipType(shipClasses);
            byType.forEach((type, classes) -> {
                final Map<String, List<WikiShipClass>> byIntroductionDate = groupByIntroduction(classes);
                final List<ClassesByIntroductionDate> collect = byIntroductionDate.entrySet().stream().map(e -> new ClassesByIntroductionDate(e.getKey(), e.getValue())).collect(Collectors.toList());
                collector.add(new ClassesByIntroductionDateAndType(type, collect));
            });
            result.add(new ClassesByAffiliation(affiliation, collector));
        });
        return result;
    }

    @Nonnull
    private Map<String, List<WikiShipClass>> groupByIntroduction(final List<WikiShipClass> wikiShipClasses) {

        final List<WikiShipClass> errors = wikiShipClasses.stream()
                .filter(c -> c.getRawValue(FieldName.Einfuehrung) == null || !Objects.requireNonNull(c.getRawValue(FieldName.Einfuehrung)).matches(S_1_PD))
                .collect(Collectors.toList());
        addErrors(FieldName.Einfuehrung, errors);

        return wikiShipClasses.stream()
                .filter(c -> c.getRawValue(FieldName.Einfuehrung) != null)
                .filter(c -> Objects.requireNonNull(c.getRawValue(FieldName.Einfuehrung)).matches(S_1_PD))
                .collect(Collectors.groupingBy(c -> c.getRawValues(FieldName.Einfuehrung).get(0),
                        Collectors.mapping(Function.identity(), Collectors.toList())));
    }

    @Nonnull
    private Map<String, List<WikiShipClass>> groupByShipType(final List<WikiShipClass> wikiShipClasses) {

        final List<WikiShipClass> errors = wikiShipClasses.stream()
                .filter(c -> c.getRawValue(FieldName.Typ) == null)
                .collect(Collectors.toList());
        addErrors(FieldName.Typ, errors);

        //noinspection DataFlowIssue
        return wikiShipClasses.stream()
                .filter(c -> c.getRawValue(FieldName.Typ) != null)
                .collect(Collectors.groupingBy(c -> c.getRawValue(FieldName.Typ),
                        Collectors.mapping(Function.identity(), Collectors.toList())));
    }

    @Nonnull
    private Map<String, List<WikiShipClass>> groupByAffiliation(final List<WikiShipClass> wikiShipClasses) {

        final List<WikiShipClass> errors = wikiShipClasses.stream()
                .filter(c -> c.getRawValue(FieldName.Zugehoerigkeit) == null)
                .collect(Collectors.toList());
        addErrors(FieldName.Zugehoerigkeit, errors);

        //noinspection DataFlowIssue
        return wikiShipClasses.stream()
                .filter(c -> c.getRawValue(FieldName.Zugehoerigkeit) != null)
                .collect(Collectors.groupingBy(c -> c.getRawValue(FieldName.Zugehoerigkeit),
                        Collectors.mapping(Function.identity(), Collectors.toList())));
    }


    private void addErrors(@Nonnull final FieldName errorType, @Nonnull final List<WikiShipClass> errors) {
        Preconditions.checkNotNull(errors, "errors must not be empty");

        if (errors.isEmpty()) {
            return;
        }
        final List<WikiShipClass> orDefault = this.errors.getOrDefault(errorType, new ArrayList<>());
        orDefault.addAll(errors);
        this.errors.put(errorType, orDefault);
    }

    private void logErrors() {
        errors.forEach((errorType, wikiShipClasses) -> {
            System.out.println(errorType + ", " + wikiShipClasses.size() + "x");
            wikiShipClasses.forEach(s -> {
                final String name = s.getRawValue(FieldName.Name);
                final List<String> rawValues = s.getRawValues(errorType);
                System.out.println(name + ", Error: " + String.join(",", rawValues));
            });
            System.out.println();
            System.out.println();
        });
    }
}
