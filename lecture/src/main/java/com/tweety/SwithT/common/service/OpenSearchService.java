package com.tweety.SwithT.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tweety.SwithT.lecture.domain.Lecture;
import com.tweety.SwithT.lecture.dto.LectureDetailResDto;
import com.tweety.SwithT.lecture.dto.LectureSearchDto;
import com.tweety.SwithT.lecture.repository.LectureRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.opensearch.OpenSearchClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class OpenSearchService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.opensearch.url}")
    private String openSearchUrl;

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${spring.opensearch.region}")
    private String region;

    @Value("${spring.opensearch.username}")
    private String username;

    @Value("${spring.opensearch.password}")
    private String password;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private final LectureRepository lectureRepository;

    @Autowired
    public OpenSearchService(LectureRepository lectureRepository) {
        this.lectureRepository = lectureRepository;
    }

    public OpenSearchClient createOpenSearchClient() {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
        return OpenSearchClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .endpointOverride(URI.create(openSearchUrl))
                .build();
    }

    @PostConstruct
    public void init() {
        try {
            ensureIndexExists("lecture-service");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void ensureIndexExists(String indexName) throws IOException, InterruptedException {
        String endpoint = openSearchUrl + "/" + indexName;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 404) {
            createIndex(indexName);
        }
    }

    // S3에서 인덱스 설정 파일을 다운로드하는 메서드
    private String downloadIndexConfigFromS3(String bucketName, String key) throws IOException {
        S3Client s3 = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        InputStream s3InputStream = s3.getObject(getObjectRequest);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(s3InputStream, StandardCharsets.UTF_8));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();

        } catch (NoSuchKeyException e) {
            System.out.println("설치 실패");
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private void createIndex(String indexName) throws IOException, InterruptedException {
        String endpoint = openSearchUrl + "/" + indexName;

        // S3에서 인덱스 설정 파일 다운로드
        String key = "lecture-service/lecture-index.json";
        String indexMapping = downloadIndexConfigFromS3(bucketName, key);
        System.out.println("다운로드된 인덱스 매핑 설정: " + indexMapping);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()))
                .PUT(HttpRequest.BodyPublishers.ofString(indexMapping))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("인덱스 생성 실패: " + response.body());
        }
    }

    public void registerLecture(LectureDetailResDto lecture) throws IOException, InterruptedException {
        String endpoint = openSearchUrl + "/lecture-service/_doc/" + lecture.getId();
        String requestBody = objectMapper.writeValueAsString(lecture);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()))
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new IOException("OpenSearch에 강의 등록 실패: " + response.body());
        }
    }

    // OpenSearch에서 강의 삭제
    public void deleteLecture(Long lectureId) throws IOException, InterruptedException {
        String endpoint = openSearchUrl + "/lecture-service/_doc/" + lectureId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("OpenSearch에서 강의 삭제 실패: " + response.body());
        }
    }

    public List<LectureDetailResDto> searchLectures(String keyword, Pageable pageable, LectureSearchDto searchDto) throws IOException, InterruptedException {
        String endpoint = openSearchUrl + "/lecture-service/_search?scroll=1m";

        // 필터 조건을 구성하기 전에 빈 값인지 확인하여 필터링 처리
        List<String> filters = new ArrayList<>();

        if (searchDto.getCategory() != null && !searchDto.getCategory().isEmpty()) {
            filters.add(String.format("{\"match\": {\"category\": \"%s\"}}", searchDto.getCategory()));
        }

        if (searchDto.getStatus() != null && !searchDto.getStatus().isEmpty()) {
            filters.add(String.format("{\"match\": {\"status\": \"%s\"}}", searchDto.getStatus()));
        }

        if (searchDto.getLectureType() != null && !searchDto.getLectureType().isEmpty()) {
            filters.add(String.format("{\"match\": {\"lectureType\": \"%s\"}}", searchDto.getLectureType()));
        }

        // 필터가 없으면 빈 배열로 처리, 있으면 join으로 연결
        String filterQuery = filters.isEmpty() ? "" : String.join(",", filters);

        // OpenSearch 요청에 페이지네이션 값 적용
        int size = 100; // `scroll` API의 최대 크기를 사용

        // OpenSearch 쿼리 생성
        String queryPart;
        if (keyword == null || keyword.isEmpty()) {
            queryPart = """
        {
            "match_all": {}
        }
        """;
        } else {
            queryPart = String.format("""
        {
            "multi_match": {
                "query": "%s",
                "fields": ["title", "contents", "memberName"],
                "analyzer": "ngram_analyzer"
            }
        }
        """, keyword);
        }

        // 필터가 있을 경우만 'filter'를 추가
        String requestBody = String.format("""
        {
                "query": {
                "bool": {
                    "must": [%s]%s
                }
            },
            "sort": [
                {"id": {"order": "desc"}}
            ],
            "size": %d
        }
        """, queryPart, filters.isEmpty() ? "" : String.format(", \"filter\": [%s]", filterQuery), size);

        // 첫 번째 요청
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("OpenSearch 검색 요청 실패: " + response.body());
        }

        // 첫 번째 응답에서 scroll_id를 가져옴
        JsonNode jsonNode = objectMapper.readTree(response.body());
        String scrollId = jsonNode.path("_scroll_id").asText();
        List<LectureDetailResDto> lectureList = parseSearchResults(response.body());

        // scroll API를 통해 계속해서 데이터를 가져옴
        while (true) {
            String scrollRequestBody = String.format("""
        {
            "scroll": "1m",
            "scroll_id": "%s"
        }
        """, scrollId);

            HttpRequest scrollRequest = HttpRequest.newBuilder()
                    .uri(URI.create(openSearchUrl + "/_search/scroll"))
                    .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(scrollRequestBody))
                    .build();

            HttpResponse<String> scrollResponse = client.send(scrollRequest, HttpResponse.BodyHandlers.ofString());

            if (scrollResponse.statusCode() != 200) {
                throw new IOException("OpenSearch scroll 요청 실패: " + scrollResponse.body());
            }

            JsonNode scrollNode = objectMapper.readTree(scrollResponse.body());
            JsonNode hits = scrollNode.path("hits").path("hits");

            if (!hits.isArray() || hits.size() == 0) {
                break; // 더 이상 가져올 데이터가 없으면 루프 종료
            }

            lectureList.addAll(parseSearchResults(scrollResponse.body()));
        }

        return lectureList;
    }

    // 카테고리로만 검색하는 메서드
    public List<LectureDetailResDto> searchLecturesByCategory(LectureSearchDto searchDto, Pageable pageable) throws IOException, InterruptedException {
        String endpoint = openSearchUrl + "/lecture-service/_search?scroll=1m";

        List<String> filters = new ArrayList<>();

        // 카테고리 필터
        if (searchDto.getCategory() != null && !searchDto.getCategory().isEmpty()) {
            filters.add(String.format("{\"match\": {\"category\": \"%s\"}}", searchDto.getCategory()));
        }

        // 상태 필터
        if (searchDto.getStatus() != null && !searchDto.getStatus().isEmpty()) {
            filters.add(String.format("{\"match\": {\"status\": \"%s\"}}", searchDto.getStatus()));
        }

        // 강의 유형 필터
        if (searchDto.getLectureType() != null && !searchDto.getLectureType().isEmpty()) {
            filters.add(String.format("{\"match\": {\"lectureType\": \"%s\"}}", searchDto.getLectureType()));
        }

        // 필터를 연결하여 쿼리 생성
        String filterQuery = filters.isEmpty() ? "" : String.join(",", filters);

        // OpenSearch 요청에 페이지네이션 값 적용
        int size = 100; // `scroll` API의 최대 크기를 사용

        // 필터가 없으면 빈 배열로 처리, 있으면 join으로 연결
        String filterPart = filters.isEmpty() ? "" : String.format(", \"filter\": [%s]", filterQuery);

        // 필터 쿼리 생성
        String requestBody = String.format("""
        {
            "query": {
                "bool": {
                    "must": []%s
                }
            },
            "sort": [
                {"id": {"order": "desc"}}
            ],
            "size": %d
        }
        """, filterPart, size);

        // 첫 번째 요청
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("OpenSearch 검색 요청 실패: " + response.body());
        }

        // 첫 번째 응답에서 scroll_id를 가져옴
        JsonNode jsonNode = objectMapper.readTree(response.body());
        String scrollId = jsonNode.path("_scroll_id").asText();
        List<LectureDetailResDto> lectureList = parseSearchResults(response.body());

        // scroll API를 통해 계속해서 데이터를 가져옴
        while (true) {
            String scrollRequestBody = String.format("""
            {
                "scroll": "1m",
                "scroll_id": "%s"
            }
            """, scrollId);

            HttpRequest scrollRequest = HttpRequest.newBuilder()
                    .uri(URI.create(openSearchUrl + "/_search/scroll"))
                    .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(scrollRequestBody))
                    .build();

            HttpResponse<String> scrollResponse = client.send(scrollRequest, HttpResponse.BodyHandlers.ofString());

            if (scrollResponse.statusCode() != 200) {
                throw new IOException("OpenSearch scroll 요청 실패: " + scrollResponse.body());
            }

            JsonNode scrollNode = objectMapper.readTree(scrollResponse.body());
            JsonNode hits = scrollNode.path("hits").path("hits");

            if (!hits.isArray() || hits.size() == 0) {
                break; // 더 이상 가져올 데이터가 없으면 루프 종료
            }

            lectureList.addAll(parseSearchResults(scrollResponse.body()));
        }

        return lectureList;
    }

    private List<LectureDetailResDto> parseSearchResults(String responseBody) throws IOException {
        List<LectureDetailResDto> lectureList = new ArrayList<>();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        JsonNode hits = jsonNode.path("hits").path("hits");

        for (JsonNode hit : hits) {
            JsonNode source = hit.path("_source");
            LectureDetailResDto lecture = objectMapper.treeToValue(source, LectureDetailResDto.class);
            lectureList.add(lecture);
        }

        // totalHits 값을 로그로 확인하거나 필요시 사용할 수 있음
        long totalHits = jsonNode.path("hits").path("total").path("value").asLong();
        System.out.println("Total Hits: " + totalHits);

        return lectureList;
    }

    public List<String> getSuggestions(String keyword) throws IOException, InterruptedException {
        String endpoint = openSearchUrl + "/lecture-service/_search";

        // 검색된 횟수를 기준으로 정렬하고 상위 10개의 검색어만 반환하는 쿼리
        String requestBody = String.format("""
        {
                "query": {
                    "bool": {
                        "should": [
                            {
                                "match": {
                                    "title": {
                                        "query": "%s",
                                        "operator": "and"
                                    }
                                }
                            },
                            {
                                "match": {
                                    "contents": {
                                        "query": "%s",
                                        "operator": "and"
                                    }
                                }
                            },
                            {
                                "match": {
                                    "memberName": {
                                        "query": "%s",
                                        "operator": "and"
                                    }
                                }
                            }
                        ]
                    }
                },
                "sort": [
                    { "search_count": { "order": "desc" } }
                ],
                "size": 10
        }
        """, keyword, keyword, keyword);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return parseSuggestions(response.body());
        } else {
            throw new IOException("추천 검색 요청 실패: " + response.body());
        }
    }

    // 응답 파싱 메서드
    private List<String> parseSuggestions(String responseBody) throws IOException {
        List<String> suggestions = new ArrayList<>();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        JsonNode hits = jsonNode.path("hits").path("hits");

        for (JsonNode hit : hits) {
            String suggestion = hit.path("_source").path("title").asText();  // 제목을 추천어로 사용
            suggestions.add(suggestion);
        }

        return suggestions;
    }

    @PostConstruct
    @Scheduled(cron = "0 */10 * * * *")
    public void syncLecturesToOpenSearch() {
        List<Lecture> lectures = lectureRepository.findAll();
        for (Lecture lecture : lectures) {
            try {
                registerLecture(lecture.fromEntityToLectureResDto());
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
