package egovframework.example.global.config;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;
class OracleReleaseEvidenceTest {
 @Test void product26aiFreeDoesNotRequireJdbcMajor26(){
  OracleConnectionOracleIT.assertTargetBanner("Oracle AI Database 26ai Free Release 23.26.0.0.0 - Develop, Learn, and Run for Free");
 }
 @ParameterizedTest @ValueSource(strings={"Oracle Database 23ai Free Release 23.4", "Oracle AI Database 26ai Enterprise Edition Release 23.26", "Other Database 26 Free"})
 void rejectsDifferentProductOrEdition(String banner){assertThatThrownBy(()->OracleConnectionOracleIT.assertTargetBanner(banner)).isInstanceOf(AssertionError.class);}
}
