package imise.encoding;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AnsiEncoderTest {

	@Test
	public void testReplaceAllAnsi() {
		String in = "f�r einen test mit ��� und � und � und weiteren kritischen � Zeichen @ ?";
		String out = AnsiEncoder.replaceAllAnsi(in);
		System.out.println(in);
		System.out.println(out);
		assertTrue(out.contains("&#252;"));
		assertTrue(out.contains("&#234;"));
	}
}
