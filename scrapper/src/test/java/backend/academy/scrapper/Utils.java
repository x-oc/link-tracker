package backend.academy.scrapper;

import java.util.Objects;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Utils {

    @SneakyThrows
    public static String readAll(String fileName) {
        return new String(Objects.requireNonNull(Utils.class.getResourceAsStream(fileName))
                .readAllBytes());
    }
}
