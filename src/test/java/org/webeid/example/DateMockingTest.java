package org.webeid.example;

import org.junit.jupiter.api.Test;
import org.webeid.example.testutil.Dates;

import java.text.ParseException;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class DateMockingTest {

    @Test
    void testDateMocking() throws ParseException {
        final Date date = Dates.create("2020-04-14T13:36:49Z");
        Dates.setMockedDate(date);

        // Mocking native methods is unreliable, see https://github.com/jmockit/jmockit1/issues/689#issuecomment-702965484
        assertThat(System.currentTimeMillis()).isEqualTo(date.getTime());
    }

}
