package imise.encoding;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AnsiEncoderTest {

	@Test
	public void testReplaceAllAnsi() {
		String in = "für einen test mit äüö und é und ê und weiteren kritischen ÿ Zeichen @ ?";
		String out = AnsiEncoder.replaceAllAnsi(in);
		System.out.println(in);
		System.out.println(out);
		assertTrue(out.contains("&#252;"));
		assertTrue(out.contains("&#234;"));
	}
}
