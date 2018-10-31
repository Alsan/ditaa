package sandbox;

import com.github.dakusui.crest.Crest;
import org.junit.Test;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;
import org.stathissideris.ascii2image.core.CommandLineConverter;
import org.stathissideris.ascii2image.graphics.DiagramText;
import org.stathissideris.ascii2image.text.StringUtils;
import org.stathissideris.ascii2image.text.TextGrid;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.function.Predicate;

import static com.github.dakusui.crest.Crest.asObject;
import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.crest.Crest.callOn;
import static java.lang.String.format;

public class Sandbox {
  @Test
  public void latexMath() throws IOException {
    String latex = "$\\sum_{i=0}^{n}x^i$";
    TeXFormula formula = new TeXFormula(latex);

    // Note: Old interface for creating icons:
    // TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 20);
    // Note: New interface using builder pattern (inner class):
    TeXIcon icon = formula.new TeXIconBuilder().setStyle(TeXConstants.STYLE_DISPLAY).setSize(20)
        .build();

    icon.setInsets(new Insets(5, 5, 5, 5));

    BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
        BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = image.createGraphics();
    g2.setColor(Color.white);
    g2.fillRect(0, 0, icon.getIconWidth(), icon.getIconHeight());
    JLabel jl = new JLabel();
    jl.setForeground(new Color(0, 0, 0));
    icon.paintIcon(jl, g2, 0, 0);
    File file = new File("target/Example1.png");
    ImageIO.write(image, "png", file.getAbsoluteFile());
  }

  @Test
  public void latexMath2() throws IOException {
    String latex = "$\\sum_{i=0}^{n}x^i$";

    BufferedImage image = new BufferedImage(
        640, 480,
        BufferedImage.TYPE_INT_ARGB
    );
    DiagramText.drawTeXFormula(image.createGraphics(), latex, 0, 0, Color.black, 20);
    File file = new File("target/Example1.png");
    ImageIO.write(image, "png", file.getAbsoluteFile());
  }

  /**
   * This method is based on a discussion <a href="https://stackoverflow.com/questions/8567905/how-to-compare-images-for-similarity-using-java">How to compare images for similarity</a> made in the Stackoverflow.com
   * If 2 images are identical, this method will return 1.0.
   *
   * @param fileA input image A
   * @param fileB input image B
   * @return The similarity.
   */
  public static double compareImages(File fileA, File fileB) {
    double percentage = 0;
    try {
      // take buffer data from both image files //
      BufferedImage biA = ImageIO.read(fileA);
      DataBuffer dbA = biA.getData().getDataBuffer();
      int sizeA = dbA.getSize();
      BufferedImage biB = ImageIO.read(fileB);
      DataBuffer dbB = biB.getData().getDataBuffer();
      int sizeB = dbB.getSize();
      int count = 0;
      // compare data-buffer objects //
      if (sizeA == sizeB) {

        for (int i = 0; i < sizeA; i++) {

          if (dbA.getElem(i) == dbB.getElem(i)) {
            count = count + 1;
          }

        }
        percentage = ((double) count) / sizeA;
      } else {
        System.out.println("Both the images are not of same size");
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return percentage;
  }


  @Test
  public void givenStringContainingLaTeXFormula4whenTransform$thenResultIsCorrect() {
    assertThat(
        TextGrid.transformRowToModeRow("xyz$xyz^^^$xyz"),
        asString().equalTo("PPPLLLLLLLLPPP").$()
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void givenStringContainingUnfinishedLaTeXFormula$whenTransform$thenIllegalArgumentException() {
    TextGrid.transformRowToModeRow("xyz$xyz^^^_xyz");
  }

  @Test
  public void givenStringNotContainingLaTeXFormula$whenTransform$thenResultIsCorrect() {
    assertThat(
        TextGrid.transformRowToModeRow("xyz_xyz^^^_xyz"),
        asString().equalTo("PPPPPPPPPPPPPP").$()
    );
  }

  @Test
  public void givenEmptyString$whenTransform$thenResultIsEmpty() {
    assertThat(
        TextGrid.transformRowToModeRow(""),
        asString().equalTo("").$()
    );
  }


  @Test
  public void testSplit() {
    String s = "hello$HELLO$ world";
    Iterator<String> i = StringUtils.createTextSplitter(DiagramText.TEXT_SPLITTING_REGEX, s);

    while (i.hasNext())
      System.out.println(i.next());
  }

  @Test
  public void renderAsLatexAsciiArt() {
    CommandLineConverter.main(new String[] { "tests/text/art-latexmath-1.txt", "-o", "--latex-math" });
  }

  @Test
  public void renderAsNonLatexAsciiArt() {
    CommandLineConverter.main(new String[] { "tests/text/art-latexmath-1.txt", "-o" });
  }

  public static BufferedImage getDifferenceImage(BufferedImage img1, BufferedImage img2) {
    // convert images to pixel arrays...
    final int w = img1.getWidth(),
        h = img1.getHeight(),
        highlight = Color.MAGENTA.getRGB();
    final int[] p1 = img1.getRGB(0, 0, w, h, null, 0, w);
    final int[] p2 = img2.getRGB(0, 0, w, h, null, 0, w);
    // compare img1 to img2, pixel by pixel. If different, highlight img1's pixel...
    for (int i = 0; i < p1.length; i++) {
      if (p1[i] != p2[i]) {
        p1[i] = highlight;
      }
    }
    // save img1's pixels to a new BufferedImage, and return it...
    // (May require TYPE_INT_ARGB)
    final BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    out.setRGB(0, 0, w, h, p1, 0, w);
    return out;
  }

  public static File getDifferenceImageFile(File img1, File img2) throws Throwable {
    File ret;
    ImageIO.write(
        getDifferenceImage(
            ImageIO.read(img1),
            ImageIO.read(img2)),
        "png",
        ret = new File("output.png"));
    return ret;
  }

  @Test
  public void testDiffImage() throws IOException {
    ImageIO.write(
        getDifferenceImage(
            ImageIO.read(new File("tests/text/art-latexmath-1.png")),
            ImageIO.read(new File("tests/text/art-latexmath-2.png"))),
        "png",
        new File("output.png"));
  }

  @Test
  public void testImages() throws Throwable {
    File actual = new File("tests/text/art-latexmath-1.png");
    File expected = new File("tests/text/art-latexmath-2.png");
    assertThat(
        actual,
        asObject().check(
            callOn(Sandbox.class, "getDifferenceImageFile", expected, actual).$(), similarImpageTo(expected, 0.999999)).$()
    );
  }

  private Predicate<File> similarImpageTo(File expected, double threshold) {
    return Crest.predicate(
        format("similarImageTo(%s,%s)", expected, threshold),
        actual -> compareImages(expected, actual) > threshold);
  }
}