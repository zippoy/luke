package org.apache.lucene.luke.models.overview;

import org.junit.Test;

import java.util.List;

public class TopTermsImplTest extends OverviewTestBase {

  @Test
  public void testGetTopTerms() throws Exception {
    TopTermsImpl topTerms = new TopTermsImpl();
    topTerms.reset(reader);
    List<TermStats> result = topTerms.getTopTerms("f2", 2);

    assertEquals("a", result.get(0).getDecodedTermText());
    assertEquals(3, result.get(0).getDocFreq());
    assertEquals("f2", result.get(0).getField());

    assertEquals("c", result.get(1).getDecodedTermText());
    assertEquals(2, result.get(1).getDocFreq());
    assertEquals("f2", result.get(1).getField());
  }

}
