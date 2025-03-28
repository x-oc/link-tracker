package backend.academy.scrapper.sender;

import backend.academy.scrapper.dto.request.LinkUpdate;

public interface LinkUpdateSender {

    void sendUpdate(LinkUpdate linkUpdate);
}
