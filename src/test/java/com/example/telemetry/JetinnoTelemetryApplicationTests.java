package com.example.telemetry;

import com.example.telemetry.model.Task;
import com.example.telemetry.service.ResponseService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class JetinnoTelemetryApplicationTests {

	@Value("${inputFrames}")
	private String inputFrames;

	@Value("${expectedFrames}")
	private String expectedFrames;

	private final ResponseService responseService;

	@Autowired
    public JetinnoTelemetryApplicationTests(ResponseService responseService) {
        this.responseService = responseService;
    }

    @Test
	public void testGeneratedFrame() throws Exception {
		List<Map.Entry<byte[], String>> requests = readFrameFromFile(inputFrames);
		List<Map.Entry<byte[], String>> expects = readFrameFromFile(expectedFrames);
		String[] strings = {"clearerror"};
		List<String> ignoredCmd = new ArrayList<>(Arrays.asList(strings));
		int j = 0;

		for (int i = 0; i < requests.size(); i++){
			Map.Entry<byte[], String> request = requests.get(i);
			Map.Entry<byte[], String> expect = expects.get(j);
			ObjectNode body = (ObjectNode) responseService.getObjectMapper().readTree(request.getValue());
			if (ignoredCmd.stream().anyMatch(request.getValue() :: contains)) {
				continue;
			}

			Task task = new Task(body.get("cmd").asText(), body);

			byte[] myResponseFrame = responseService.processTelemetry(task);

			byte[] myHeader = new byte[12];
			System.arraycopy(myResponseFrame, 0, myHeader, 0, 12);
			for (int l = 0; l < myHeader.length; l++) {
				System.out.print(myHeader[l]);
			}
			System.out.println();
			//int[] arr = Arrays.stream(myHeader)
			//		.map(x -> x & 0xFF);
			//Stream<Byte> byteStream = IntStream.range(0, myHeader.length)
			//		.mapToObj(x -> myHeader[x] & 0xFF);
			String myResponseBody = responseService.generateResponseBody(task);


			//assertTrue(Arrays.equals(expect.getKey(), myHeader));

			boolean isEqual = compareJsonIgnoringFields(myResponseBody, expect.getValue(), "timestamp", "date_time", "server_list");

			assertTrue(isEqual);
			j++;
		}
	}

	private List<Map.Entry<byte[], String>> readFrameFromFile(String filename) throws Exception {
		byte[] content = Files.readAllBytes(Paths.get(filename));
		byte[] headerMarker = "Header : ".getBytes();
		byte[] bodyMarker = "Body : ".getBytes();

		List<Map.Entry<byte[], String>> frames = new ArrayList<>();

		int i = 0;
		while (i < content.length) {

			int headerStart = indexOf(content, headerMarker, i);
			if (headerStart == -1) break;


			int headerPos = headerStart + headerMarker.length;

			byte[] header = new byte[4];
			System.arraycopy(content, headerPos, header, 0, 4);

			int bodyStart = indexOf(content, bodyMarker, headerPos + 4);
			if (bodyStart == -1) break;

			int bodyPos = bodyStart + bodyMarker.length;

			int bodyEnd = indexOf(content, "}".getBytes(), bodyPos);
			if (bodyEnd == -1) break;


			String body = new String(content, bodyPos, bodyEnd - bodyPos + 1);


			frames.add(new AbstractMap.SimpleEntry<>(header, body));


			i = bodyEnd + 1;
		}
		return frames;

	}

	private static int indexOf(byte[] content, byte[] target, int start) {
		outer:
		for (int i = start; i <= content.length - target.length; i++) {
			for (int j = 0; j < target.length; j++) {
				if (content[i + j] != target[j]) {
					continue outer;
				}
			}
			return i;
		}
		return -1;  // Не найдено
	}


	private boolean compareJsonIgnoringFields(String json1, String json2, String... fieldsToIgnore) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode tree1 = objectMapper.readTree(json1);
		JsonNode tree2 = objectMapper.readTree(json2);

		for (String field : fieldsToIgnore) {
			((com.fasterxml.jackson.databind.node.ObjectNode) tree1).remove(field);
			((com.fasterxml.jackson.databind.node.ObjectNode) tree2).remove(field);
		}

		return tree1.equals(tree2);
	}
}
