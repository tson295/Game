package Main;

import javax.sound.sampled.*;

/**
 * Hệ thống âm thanh – sinh âm tổng hợp bằng javax.sound.sampled.
 * Không cần file audio bên ngoài.
 */
public class Sound {

    public static void play(String name) {
        new Thread(() -> {
            try {
                float sampleRate = 44100f;
                int channels  = 1;
                int bits      = 16;
                boolean signed = true;
                boolean bigEndian = false;
                AudioFormat fmt = new AudioFormat(sampleRate, bits, channels, signed, bigEndian);

                byte[] buf = generate(name, sampleRate);
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
                if (!AudioSystem.isLineSupported(info)) return;

                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(fmt);
                line.start();
                line.write(buf, 0, buf.length);
                line.drain();
                line.close();
            } catch (Exception ignored) { /* headless / audio unavailable */ }
        }, "SoundThread").start();
    }

    // ---------------------------------------------------------------
    private static byte[] generate(String name, float sr) {
        return switch (name) {
            case "pickup"  -> tone(sr, 0.10, 880, 1320, 0.25f);
            case "door"    -> tone(sr, 0.25, 440,  660, 0.3f);
            case "attack"  -> noise(sr, 0.08, 0.35f);
            case "hit"     -> tone(sr, 0.12, 220,  110, 0.4f);
            case "die"     -> tone(sr, 0.50, 300,   80, 0.4f);
            case "win"     -> winJingle(sr);
            case "nope"    -> tone(sr, 0.10, 200,  200, 0.2f);
            default        -> new byte[0];
        };
    }

    /** Sweeping sine from f0 → f1 with linear frequency interpolation */
    private static byte[] tone(float sr, double dur, double f0, double f1, float amp) {
        int n = (int)(sr * dur);
        byte[] buf = new byte[n * 2];
        double phase = 0;
        for (int i = 0; i < n; i++) {
            double t  = (double) i / n;
            double freq = f0 + (f1 - f0) * t;
            double env  = Math.sin(Math.PI * t);      // fade in & out
            phase += 2 * Math.PI * freq / sr;
            short s = (short)(Math.sin(phase) * env * amp * Short.MAX_VALUE);
            buf[i * 2]     = (byte)(s & 0xFF);
            buf[i * 2 + 1] = (byte)((s >> 8) & 0xFF);
        }
        return buf;
    }

    /** Short burst of white noise – attack/slash sound */
    private static byte[] noise(float sr, double dur, float amp) {
        int n = (int)(sr * dur);
        byte[] buf = new byte[n * 2];
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < n; i++) {
            double env = Math.sin(Math.PI * (double) i / n);
            short s = (short)((rnd.nextDouble() * 2 - 1) * env * amp * Short.MAX_VALUE);
            buf[i * 2]     = (byte)(s & 0xFF);
            buf[i * 2 + 1] = (byte)((s >> 8) & 0xFF);
        }
        return buf;
    }

    /** Win jingle: 3 rising notes */
    private static byte[] winJingle(float sr) {
        byte[] a = tone(sr, 0.18, 523, 523, 0.3f);
        byte[] b = tone(sr, 0.18, 659, 659, 0.3f);
        byte[] c = tone(sr, 0.35, 784, 784, 0.35f);
        byte[] d = tone(sr, 0.45, 1046, 1046, 0.3f);
        byte[] out = new byte[a.length + b.length + c.length + d.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        System.arraycopy(c, 0, out, a.length + b.length, c.length);
        System.arraycopy(d, 0, out, a.length + b.length + c.length, d.length);
        return out;
    }
}
