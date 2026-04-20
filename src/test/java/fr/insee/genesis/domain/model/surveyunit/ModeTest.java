package fr.insee.genesis.domain.model.surveyunit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModeTest {

    // -------------------------------------------------------------------------
    // Enum values & properties
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Enum constants and their properties")
    class EnumConstantsTests {

        @Test
        @DisplayName("WEB should have correct properties")
        void web_shouldHaveCorrectProperties() {
            assertThat(Mode.WEB.getModeName()).isEqualTo("WEB");
            assertThat(Mode.WEB.getFolder()).isEqualTo("WEB");
            assertThat(Mode.WEB.getJsonName()).isEqualTo("CAWI");
        }

        @Test
        @DisplayName("TEL should have correct properties")
        void tel_shouldHaveCorrectProperties() {
            assertThat(Mode.TEL.getModeName()).isEqualTo("TEL");
            assertThat(Mode.TEL.getFolder()).isEqualTo("ENQ");
            assertThat(Mode.TEL.getJsonName()).isEqualTo("CATI");
        }

        @Test
        @DisplayName("F2F should have correct properties")
        void f2f_shouldHaveCorrectProperties() {
            assertThat(Mode.F2F.getModeName()).isEqualTo("F2F");
            assertThat(Mode.F2F.getFolder()).isEqualTo("ENQ");
            assertThat(Mode.F2F.getJsonName()).isEqualTo("CAPI");
        }

        @Test
        @DisplayName("PAPER should have correct properties")
        void paper_shouldHaveCorrectProperties() {
            assertThat(Mode.PAPER.getModeName()).isEqualTo("PAPER");
            assertThat(Mode.PAPER.getFolder()).isEmpty();
            assertThat(Mode.PAPER.getJsonName()).isEqualTo("PAPI");
        }

        @Test
        @DisplayName("OTHER should have empty folder and jsonName")
        void other_shouldHaveEmptyFolderAndJsonName() {
            assertThat(Mode.OTHER.getModeName()).isEqualTo("OTHER");
            assertThat(Mode.OTHER.getFolder()).isEmpty();
            assertThat(Mode.OTHER.getJsonName()).isEmpty();
        }

        @Test
        @DisplayName("Enum should have exactly 5 values")
        void enum_shouldHaveFiveValues() {
            assertThat(Mode.values()).hasSize(5);
        }
    }

    // -------------------------------------------------------------------------
    // fromString()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("fromString() tests")
    class FromStringTests {

        @ParameterizedTest(name = "fromString(\"{0}\") should return {1}")
        @CsvSource({
                "WEB,   WEB",
                "TEL,   TEL",
                "F2F,   F2F",
                "PAPER, PAPER",
                "OTHER, OTHER",
                // JSON names
                "CAWI,  WEB",
                "CATI,  TEL",
                "CAPI,  F2F",
                "PAPI,  PAPER",
        })
        @DisplayName("fromString should resolve by modeName or jsonName")
        void fromString_shouldResolveKnownNames(String input, Mode expected) {
            assertThat(Mode.fromString(input)).isEqualTo(expected);
        }

        @ParameterizedTest(name = "fromString(\"{0}\") should be case-insensitive")
        @ValueSource(strings = {"web", "Web", "wEb", "cawi", "Cawi", "CAWI", "tel", "Tel"})
        @DisplayName("fromString should be case-insensitive")
        void fromString_shouldBeCaseInsensitive(String input) {
            assertThat(Mode.fromString(input)).isNotNull();
        }

        @ParameterizedTest(name = "fromString(\"{0}\") should handle leading/trailing spaces")
        @ValueSource(strings = {" WEB", "WEB ", " WEB ", " CAWI "})
        @DisplayName("fromString should trim whitespace")
        void fromString_shouldTrimInput(String input) {
            assertThat(Mode.fromString(input)).isNotNull();
        }

        @Test
        @DisplayName("fromString(null) should return null")
        void fromString_null_shouldReturnNull() {
            assertThat(Mode.fromString(null)).isNull();
        }

        @ParameterizedTest(name = "fromString(\"{0}\") should throw IllegalArgumentException")
        @ValueSource(strings = {"UNKNOWN", "invalid", "123", "WE B", "ENQ"})
        @DisplayName("fromString with unknown value should throw IllegalArgumentException")
        void fromString_unknownValue_shouldThrow(String input) {
            assertThatThrownBy(() -> Mode.fromString(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid Mode")
                    .hasMessageContaining(input);
        }

        @Test
        @DisplayName("fromString with blank string should throw IllegalArgumentException")
        void fromString_blankString_shouldThrow() {
            assertThatThrownBy(() -> Mode.fromString("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid Mode");
        }

        @Test
        @DisplayName("fromString with empty string should throw IllegalArgumentException")
        void fromString_emptyString_shouldThrow() {
            assertThatThrownBy(() -> Mode.fromString(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // -------------------------------------------------------------------------
    // getEnumFromModeName()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getEnumFromModeName() tests")
    class GetEnumFromModeNameTests {

        @ParameterizedTest(name = "getEnumFromModeName(\"{0}\") should return {1}")
        @CsvSource({
                "WEB,   WEB",
                "TEL,   TEL",
                "F2F,   F2F",
                "PAPER, PAPER",
                "OTHER, OTHER",
        })
        @DisplayName("getEnumFromModeName should resolve known mode names")
        void getEnumFromModeName_shouldResolveKnownNames(String input, Mode expected) {
            assertThat(Mode.getEnumFromModeName(input)).isEqualTo(expected);
        }

        @ParameterizedTest(name = "getEnumFromModeName(\"{0}\") should be case-insensitive")
        @ValueSource(strings = {"web", "Web", "WEB", "tel", "f2f", "paper"})
        @DisplayName("getEnumFromModeName should be case-insensitive")
        void getEnumFromModeName_shouldBeCaseInsensitive(String input) {
            assertThat(Mode.getEnumFromModeName(input)).isNotNull();
        }

        @Test
        @DisplayName("getEnumFromModeName(null) should return null")
        void getEnumFromModeName_null_shouldReturnNull() {
            assertThat(Mode.getEnumFromModeName(null)).isNull();
        }

        @Test
        @DisplayName("getEnumFromModeName should trim whitespace")
        void getEnumFromModeName_shouldTrimInput() {
            assertThat(Mode.getEnumFromModeName(" WEB ")).isEqualTo(Mode.WEB);
        }

        @ParameterizedTest(name = "getEnumFromModeName(\"{0}\") should return null for JSON names")
        @ValueSource(strings = {"CAWI", "CATI", "CAPI", "PAPI"})
        @DisplayName("getEnumFromModeName should NOT resolve JSON names")
        void getEnumFromModeName_jsonName_shouldReturnNull(String jsonName) {
            assertThat(Mode.getEnumFromModeName(jsonName)).isNull();
        }

        @Test
        @DisplayName("getEnumFromModeName with unknown value should return null")
        void getEnumFromModeName_unknownValue_shouldReturnNull() {
            assertThat(Mode.getEnumFromModeName("UNKNOWN")).isNull();
        }
    }

    // -------------------------------------------------------------------------
    // getEnumFromJsonName()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getEnumFromJsonName() tests")
    class GetEnumFromJsonNameTests {

        @ParameterizedTest(name = "getEnumFromJsonName(\"{0}\") should return {1}")
        @CsvSource({
                "CAWI, WEB",
                "CATI, TEL",
                "CAPI, F2F",
                "PAPI, PAPER",
        })
        @DisplayName("getEnumFromJsonName should resolve known JSON names")
        void getEnumFromJsonName_shouldResolveKnownJsonNames(String input, Mode expected) {
            assertThat(Mode.getEnumFromJsonName(input)).isEqualTo(expected);
        }

        @ParameterizedTest(name = "getEnumFromJsonName(\"{0}\") should be case-insensitive")
        @ValueSource(strings = {"cawi", "Cawi", "CAWI", "cati", "capi", "papi"})
        @DisplayName("getEnumFromJsonName should be case-insensitive")
        void getEnumFromJsonName_shouldBeCaseInsensitive(String input) {
            assertThat(Mode.getEnumFromJsonName(input)).isNotNull();
        }

        @Test
        @DisplayName("getEnumFromJsonName(null) should return null")
        void getEnumFromJsonName_null_shouldReturnNull() {
            assertThat(Mode.getEnumFromJsonName(null)).isNull();
        }

        @Test
        @DisplayName("getEnumFromJsonName should trim whitespace")
        void getEnumFromJsonName_shouldTrimInput() {
            assertThat(Mode.getEnumFromJsonName(" CAWI ")).isEqualTo(Mode.WEB);
        }

        @ParameterizedTest(name = "getEnumFromJsonName(\"{0}\") should return null for mode names")
        @ValueSource(strings = {"WEB", "TEL", "F2F", "PAPER"})
        @DisplayName("getEnumFromJsonName should NOT resolve mode names")
        void getEnumFromJsonName_modeName_shouldReturnNull(String modeName) {
            assertThat(Mode.getEnumFromJsonName(modeName)).isNull();
        }

        @Test
        @DisplayName("getEnumFromJsonName with unknown value should return null")
        void getEnumFromJsonName_unknownValue_shouldReturnNull() {
            assertThat(Mode.getEnumFromJsonName("UNKNOWN")).isNull();
        }

        @Test
        @DisplayName("getEnumFromJsonName for OTHER (empty jsonName) should return null")
        void getEnumFromJsonName_other_shouldReturnNull() {
            // OTHER has an empty jsonName, so it is not indexed
            assertThat(Mode.getEnumFromJsonName("OTHER")).isNull();
        }
    }

    // -------------------------------------------------------------------------
    // Lookup map integrity (cross-cutting)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Lookup map integrity tests")
    class LookupMapIntegrityTests {

        @Test
        @DisplayName("BY_ANY_NAME should resolve both modeName and jsonName for WEB")
        void byAnyName_shouldResolveWebByBothNames() {
            assertThat(Mode.fromString("WEB")).isEqualTo(Mode.WEB);
            assertThat(Mode.fromString("CAWI")).isEqualTo(Mode.WEB);
        }

        @Test
        @DisplayName("BY_ANY_NAME should resolve both modeName and jsonName for TEL")
        void byAnyName_shouldResolveTelByBothNames() {
            assertThat(Mode.fromString("TEL")).isEqualTo(Mode.TEL);
            assertThat(Mode.fromString("CATI")).isEqualTo(Mode.TEL);
        }

        @Test
        @DisplayName("BY_ANY_NAME should resolve both modeName and jsonName for F2F")
        void byAnyName_shouldResolveF2FByBothNames() {
            assertThat(Mode.fromString("F2F")).isEqualTo(Mode.F2F);
            assertThat(Mode.fromString("CAPI")).isEqualTo(Mode.F2F);
        }

        @Test
        @DisplayName("BY_ANY_NAME should resolve both modeName and jsonName for PAPER")
        void byAnyName_shouldResolvePaperByBothNames() {
            assertThat(Mode.fromString("PAPER")).isEqualTo(Mode.PAPER);
            assertThat(Mode.fromString("PAPI")).isEqualTo(Mode.PAPER);
        }

        @Test
        @DisplayName("OTHER has blank folder and jsonName — not resolvable by jsonName")
        void other_notResolvableByJsonName() {
            assertThat(Mode.getEnumFromJsonName("")).isNull();
            assertThat(Mode.getEnumFromJsonName("OTHER")).isNull();
        }

        @Test
        @DisplayName("TEL and F2F share folder ENQ — folders are not used for lookup")
        void telAndF2F_shareFolder_butFolderIsNotLookupKey() {
            // folder is purely a property, not a lookup key
            assertThat(Mode.TEL.getFolder()).isEqualTo(Mode.F2F.getFolder()).isEqualTo("ENQ");
            // resolving by "ENQ" is not supported
            assertThatThrownBy(() -> Mode.fromString("ENQ"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}