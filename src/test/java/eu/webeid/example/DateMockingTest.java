/*
 * Copyright (c) 2020-2023 Estonian Information System Authority
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package eu.webeid.example;

import eu.webeid.example.testutil.Dates;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static eu.webeid.example.testutil.Dates.getSigningDateTime;
import static org.assertj.core.api.Assertions.assertThat;

public class DateMockingTest {

    @Test
    void testDateMocking() {
        final ZonedDateTime signingDateTime = getSigningDateTime();
        Dates.setMockedDate(signingDateTime);

        // Mocking native methods is unreliable, see https://github.com/jmockit/jmockit1/issues/689#issuecomment-702965484
        assertThat(System.currentTimeMillis()).isEqualTo(signingDateTime.toInstant().toEpochMilli());
    }

}
