package backend.academy.scrapper.sender;

import backend.academy.scrapper.dto.request.LinkUpdate;

public interface ReliableLinkUpdateSender {

    void sendUpdateReliably(LinkUpdate linkUpdate);
}
