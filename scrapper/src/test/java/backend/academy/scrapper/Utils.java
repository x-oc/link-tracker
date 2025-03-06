package backend.academy.scrapper;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import java.util.Objects;

@UtilityClass
public class Utils {

    @SneakyThrows
    public static String readAll(String fileName) {
        return new String(Objects.requireNonNull(Utils.class.getResourceAsStream(fileName)).readAllBytes());
    }
}
