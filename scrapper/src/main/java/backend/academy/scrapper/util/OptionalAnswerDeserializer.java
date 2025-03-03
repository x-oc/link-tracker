package backend.academy.scrapper.util;

import backend.academy.scrapper.dto.OptionalAnswer;
import backend.academy.scrapper.dto.response.ApiErrorResponse;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;

public class OptionalAnswerDeserializer extends JsonDeserializer<OptionalAnswer<?>> implements ContextualDeserializer {
    private JavaType valueType;

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        JavaType wrapperType = ctxt.getContextualType();
        JavaType type = wrapperType.containedType(0);
        OptionalAnswerDeserializer deserializer = new OptionalAnswerDeserializer();
        deserializer.valueType = type;
        return deserializer;
    }

    @Override
    public OptionalAnswer<?> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
        throws IOException {
        ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        ObjectNode objectNode = mapper.readTree(jsonParser);
        if (objectNode.get("exceptionName") != null) {
            return OptionalAnswer.error(mapper.convertValue(objectNode, ApiErrorResponse.class));
        }
        if (valueType.isTypeOrSubTypeOf(Void.TYPE)) {
            return OptionalAnswer.of(null);
        }
        return OptionalAnswer.of(mapper.convertValue(objectNode, valueType));
    }
}
