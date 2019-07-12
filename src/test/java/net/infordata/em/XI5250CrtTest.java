package net.infordata.em;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.awt.Rectangle;
import net.infordata.em.crt5250.XI5250Crt;
import net.infordata.em.crt5250.XI5250Field;
import org.junit.Before;
import org.junit.Test;


public class XI5250CrtTest {

  private static final int COLUMNS = 80;
  private static final int ROWS = 24;
  private XI5250Crt xi5250Crt;

  @Before
  public void setup() {
    xi5250Crt = new XI5250Crt();
    xi5250Crt.setCrtSize(COLUMNS, ROWS);
    buildScreen();
  }

  private void buildScreen() {

    XI5250Field field1 = new XI5250Field(xi5250Crt, 2, 2, 9, 1);
    XI5250Field field2 = new XI5250Field(xi5250Crt, 3, 4, 4, 1);
    XI5250Field field3 = new XI5250Field(xi5250Crt, 2, 6, 9, 1);
    xi5250Crt.addField(field1);
    xi5250Crt.addField(field2);
    xi5250Crt.addField(field3);
    xi5250Crt.drawString("********", 2, 2);
    xi5250Crt.drawString("TEST", 3, 4);
    xi5250Crt.drawString("********", 2, 6);
  }

  @Test
  public void shouldMatchObtainedTextWithStringSelectedAreaWhenGetStringSelectedArea() {
    xi5250Crt.setSelectedArea(new Rectangle(3, 4, 4, 1));
    assertThat(xi5250Crt.getStringSelectedArea()).isEqualTo("TEST");
  }
}
