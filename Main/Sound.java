package Main;

import javax.sound.sampled.*;

/**
 * Hệ thống âm thanh tổng hợp – không cần file audio bên ngoài.
 */
public class Sound {

    public static void play(String name) {
        new Thread(() -> {
            try {
                float sr = 44100f;
                AudioFormat fmt = new AudioFormat(sr, 16, 1, true, false);
                byte[] buf = generate(name, sr);
                if (buf.length == 0) return;
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
                if (!AudioSystem.isLineSupported(info)) return;
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(fmt); line.start();
                line.write(buf, 0, buf.length);
                line.drain(); line.close();
            } catch (Exception ignored) { }
        }, "Sound-" + name).start();
    }

    private static byte[] generate(String name, float sr) {
        return switch (name) {
            case "pickup"  -> tone(sr, 0.10, 880, 1320, 0.25f);
            case "door"    -> tone(sr, 0.25, 440,  660, 0.30f);
            case "attack"  -> noise(sr, 0.08, 0.35f);
            case "hit"     -> tone(sr, 0.12, 220,  110, 0.40f);
            case "die"     -> tone(sr, 0.50, 300,   80, 0.40f);
            case "win"     -> winJingle(sr);
            case "nope"    -> tone(sr, 0.10, 200,  200, 0.20f);
            case "levelup" -> levelUpJingle(sr);
            case "equip"   -> tone(sr, 0.15, 600,  900, 0.30f);
            case "coin"    -> tone(sr, 0.07, 1100, 1400, 0.20f);
            case "portal"  -> portalSound(sr);
            case "shoot"   -> noise(sr, 0.06, 0.25f);
            case "shop"    -> tone(sr, 0.20, 550,  700, 0.25f);
            case "save"    -> tone(sr, 0.20, 440,  523, 0.25f);
            default        -> new byte[0];
        };
    }

    private static byte[] tone(float sr, double dur, double f0, double f1, float amp) {
        int n = (int)(sr * dur);
        byte[] buf = new byte[n * 2];
        double phase = 0;
        for (int i = 0; i < n; i++) {
            double t    = (double) i / n;
            double freq = f0 + (f1 - f0) * t;
            double env  = Math.sin(Math.PI * t);
            phase += 2 * Math.PI * freq / sr;
            short s = (short)(Math.sin(phase) * env * amp * Short.MAX_VALUE);
            buf[i * 2]     = (byte)(s & 0xFF);
            buf[i * 2 + 1] = (byte)((s >> 8) & 0xFF);
        }
        return buf;
    }

    private static byte[] noise(float sr, double dur, float amp) {
        int n = (int)(sr * dur);
        byte[] buf = new byte[n * 2];
        java.util.Random rnd = new java.util.Random(42);
        for (int i = 0; i < n; i++) {
            double env = Math.sin(Math.PI * (double) i / n);
            short s = (short)((rnd.nextDouble() * 2 - 1) * env * amp * Short.MAX_VALUE);
            buf[i * 2]     = (byte)(s & 0xFF);
            buf[i * 2 + 1] = (byte)((s >> 8) & 0xFF);
        }
        return buf;
    }

    private static byte[] concat(byte[]... parts) {
        int total = 0;
        for (byte[] p : parts) total += p.length;
        byte[] out = new byte[total];
        int pos = 0;
        for (byte[] p : parts) { System.arraycopy(p, 0, out, pos, p.length); pos += p.length; }
        return out;
    }

    private static byte[] winJingle(float sr) {
        return concat(tone(sr,0.18,523,523,0.3f), tone(sr,0.18,659,659,0.3f),
                      tone(sr,0.35,784,784,0.35f), tone(sr,0.45,1046,1046,0.3f));
    }

    private static byte[] levelUpJingle(float sr) {
        return concat(tone(sr,0.12,523,523,0.3f), tone(sr,0.12,659,659,0.3f),
                      tone(sr,0.12,784,784,0.3f), tone(sr,0.30,1046,1046,0.35f));
    }

    private static byte[] portalSound(float sr) {
        return concat(tone(sr,0.15,200,800,0.3f), tone(sr,0.15,800,400,0.2f),
                      tone(sr,0.20,600,1200,0.25f));
    }
}
