package backend.academy.scrapper.api;

import java.net.URI;
import java.time.OffsetDateTime;

public interface InformationProvider {

    boolean isSupported(URI url);

    String getType();

    LinkInformation fetchInformation(URI url);

    LinkInformation filter(LinkInformation info, OffsetDateTime after, String optionalMetaInfo);

    default LinkInformation filter(LinkInformation info, OffsetDateTime after) {
        return filter(info, after, null);
    }

}
