package com.alimmit.golf.courses.client;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultGolfCourseApiClientImplIT {

    private GolfCourseApiClient client;

    @BeforeEach
    void setUp() {
        String apiKey = System.getenv().get("GOLF_COURSE_API_KEY");
        client = new DefaultGolfCourseApiClientImpl("https://api.golfcourseapi.com", apiKey);
    }

    @Test
    void testFetch() {
        GolfCourse course = client.fetch(26095L);
        Assertions.assertThat(course).isNotNull()
                .hasFieldOrPropertyWithValue("clubName", "Ironwood Golf Course")
                .hasFieldOrPropertyWithValue("courseName", "Meath / Birr");
    }

    @Test
    void testSearch() {
        List<GolfCourse> golfCourses = client.search("Ironwood");
        Assertions.assertThat(golfCourses).isNotEmpty();
    }
}
