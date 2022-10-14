package de.yuga.spacebattle.backend.enums;

import com.mifmif.common.regex.Generex;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public enum EStarSystemCatalogue {

    HD("HD 226868", "[H][D] [1-9]{4,6}"),
    H("H 1956+350", "[H] [1-9]{3,4}[+] [1-9]{3}"),
    SAO("SAO 69181", "[S][A][O] [1-9]{5}"),
    AAA("3A 1956+350", "[3][A] [1-9]{4}[+][1-9]{3}"),
    SBC7("SBC7 776", "[S][B][C][7] [1-9]{3}"),
    WEB("WEB 17338", "[W][E][B] [1-9]{5}"),
    AG("AG+35 1910", "[A][G][+][1-9]{2} [1-9]{4}"),
    HIC("HIC 98298", "[H][I][C] [1-9]{4,5}"),
    SBC9("SBC9 1193", "[S][B][C][9] [1-9]{4}"),
    XRS("1XRS 19564+350", "[1][X][R][S] [1-9]{4,6}[+][1-9]{3}"),
    ALS("ALS 10678", ""),
    Hilt("Hilt 849", "[H][i][l][t] [1-9]{3}"),
    BD("BD+34 3815", "[B][D][+][1-9]{2} [1-9]{4}"),
    HIP("HIP 98298", ""),
    SWIFT("SWIFT J1958.4+3510", "[S][W][I][F][T] [J][1-9]{4}[\\.][4][+][1-9]{4}"),
    AAA2018("[AAA2018] FGL J1958.6+3510", ""),
    CGO("CGO 548", ""),
    A("[BM83] X1956+350", "[\\[][B][M][8][3][\\]] [X][1-9]{4}[+][1-9]{3}"),
    EE("2E 4306", ""),
    INTREF("INTREF 1001", ""),
    TIC("TIC 102604645", ""),
    KRL("[KRL2007b] 370", ""),
    EE2("2E 1956.4+3503", ""),
    LS("LS II +35 8", ""),
    TYC("TYC 2678-791-1", ""),
    FGL("1FGL J1958.9+3459", ""),
    M("1M 1956+350", ""),
    UU("2U 1956+35", ""),
    AAVSO("AAVSO 1954+34", ""),
    GCRV("GCRV 12319", ""),
    MASS("2MASS J19582166+3512057", "2MASS J[1-9]{8}[+] [1-9]{7}"),
    UUU("3U 1956+35", ""),
    GEN("GEN# +1.00226868", ""),
    MCW("MCW 770", ""),
    UUUU("4U 1956+35", ""),
    Gaia("Gaia DR2 2059383668236814720", ""),
    GOS("GOS G071.34+03.07 01", ""),
    PBC("PBC J1958.3+3512", ""),
    UBV("UBV 17047", ""),
    GSC("GSC 02678-00791", "GSC [1-9]{5}[-][1-9]{5}"),
    PPM("PPM 83929", ""),
    UBVM("UBV M 27507", ""),
    H19("1H 1956+350", ""),
    RXS("1RXS J195821.9+351156", "1RXS J[1-9]{5}[\\.][1-9]{1}[+][1-9]{7}");

    private final String catalogue;
    private final String pseudoRegex;

    EStarSystemCatalogue(final String catalogue, final String pseudoRegex) {
        this.catalogue = catalogue;
        this.pseudoRegex = pseudoRegex;
    }

    public String getCatalogue() {
        return catalogue;
    }

    public String getPseudoRegex() {
        return pseudoRegex;
    }

    public String generateCatalogueName() {
        final Generex generex = new Generex(pseudoRegex);
        return generex.random();
    }

    public static EStarSystemCatalogue getRandom() {
        final List<EStarSystemCatalogue> values = Arrays.stream(EStarSystemCatalogue.values())
                .filter(c -> StringUtils.isNotBlank(c.getPseudoRegex()))
                .collect(Collectors.toList());
        final int rand = ThreadLocalRandom.current().nextInt(0, values.size() - 1);
        return values.get(rand);
    }

    public static String getRandomCatalogueName() {
        final List<EStarSystemCatalogue> values = Arrays.stream(EStarSystemCatalogue.values())
                .filter(c -> StringUtils.isNotBlank(c.getPseudoRegex()))
                .collect(Collectors.toList());
        final int rand = ThreadLocalRandom.current().nextInt(0, values.size() - 1);
        return values.get(rand).generateCatalogueName();
    }
}
