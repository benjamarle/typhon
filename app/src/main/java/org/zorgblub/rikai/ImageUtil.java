package org.zorgblub.rikai;

import android.graphics.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import fuku.eb4j.util.ByteUtil;

/**
 * イメージ操作ユーティリティクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class ImageUtil {

    /** ログ */
    private static final Logger _logger = LoggerFactory.getLogger(ImageUtil.class);

    /** PNGヘッダ */
    private static final byte[] PNG_HEADER = {
        /* PNGファイルシグネチャ */
        (byte)0x89, 'P', 'N', 'G', 0x0d, 0x0a, 0x1a, 0x0a,

        /* イメージヘッダ */
        // データ長
        0x00, 0x00, 0x00, 0x0d,
        // ブロックタイプ (イメージヘッダ)
        'I', 'H', 'D', 'R',
        // イメージの幅
        0x00, 0x00, 0x00, 0x00,
        // イメージの高さ
        0x00, 0x00, 0x00, 0x00,
        // ビット数、カラータイプ
        0x08, 0x06,
        // 圧縮方式、フィルター方式、インタレース方式
        0x00, 0x00, 0x00,
        // CRC
        0x00, 0x00, 0x00, 0x00
    };

    /** PNGフッタ */
    private static final byte[] PNG_FOOTER = {
        // データ長
        0x00, 0x00, 0x00, 0x00,
        // ブロックタイプ (イメージ終端子)
        'I', 'E', 'N', 'D',
        // CRC
        (byte)0xae, 0x42, 0x60, (byte)0x82
    };


    /**
     * コンストラクタ。
     *
     */
    private ImageUtil() {
        super();
    }


    /**
     * デフォルトの圧縮レベル、前景色を黒、背景色を白、無透過で
     * ビットマップイメージをPNG (Portable Network Graphics) に変換します。
     *
     * @param b ビットマップデータ
     * @param width 画像の幅
     * @param height 画像の高さ
     * @return PNGデータ
     */
    public static byte[] bitmapToPNG(byte[] b, int width, int height) {
        return bitmapToPNG(b, width, height,
                           Color.BLACK, Color.WHITE,
                           false, Deflater.DEFAULT_COMPRESSION);
    }

    /**
     * デフォルトの圧縮レベル、指定された前景色、指定された背景色で
     * ビットマップイメージをPNG (Portable Network Graphics) に変換します。
     *
     * @param b ビットマップデータ
     * @param width 画像の幅
     * @param height 画像の高さ
     * @param foreground 前景色
     * @param background 背景色
     * @param transparent 背景を透過させるかどうか
     * @return PNGデータ
     */
    public static byte[] bitmapToPNG(byte[] b, int width, int height,
                                     int foreground, int background,
                                     boolean transparent) {
        return bitmapToPNG(b, width, height,
                           foreground, background,
                           transparent, Deflater.DEFAULT_COMPRESSION);
    }

    /**
     * 指定された圧縮レベル、指定された前景色、指定された背景色で
     * ビットマップイメージをPNG (Portable Network Graphics) に変換します。
     *
     * @param b ビットマップデータ
     * @param width 画像の幅
     * @param height 画像の高さ
     * @param foreground 前景色
     * @param background 背景色
     * @param transparent 背景を透過させるかどうか
     * @param level 圧縮レベル (0-9)
     * @return PNGデータ
     */
    public static byte[] bitmapToPNG(byte[] b, int width, int height,
                                     int foreground, int background,
                                     boolean transparent, int level) {
        
        byte[] fRGB = new byte[4];
        fRGB[0] = (byte)Color.red(foreground);
        fRGB[1] = (byte)Color.green(foreground);
        fRGB[2] = (byte)Color.blue(foreground);
        fRGB[3] = (byte)0xff;
        byte[] bRGB = new byte[4];
        bRGB[0] = (byte)Color.red(background);
        bRGB[1] = (byte)Color.green(background);
        bRGB[2] = (byte)Color.blue(background);
        if (transparent) {
            bRGB[3] = (byte)0x00;
        } else {
            bRGB[3] = (byte)0xff;
        }

        // イメージデータの作成
        byte[] image = new byte[(width*4+1)*height];
        Arrays.fill(image, (byte)0x00);

        int offi = 0;
        int offb = 0;
        byte[] c = null;
        for (int y=0; y<height; y++) {
            image[offi++] = 0x00; // filter type
            for (int x=0; x<width; x+=8) {
                int cnt = 8;
                if (x+8 > width) {
                    cnt = width - x;
                }
                int mask = 0x80;
                for (int i=0; i<cnt; i++) {
                    if ((b[offb] & mask) > 0) {
                        c = fRGB;
                    } else {
                        c = bRGB;
                    }
                    image[offi++] = c[0]; // R
                    image[offi++] = c[1]; // G
                    image[offi++] = c[2]; // B
                    image[offi++] = c[3]; // alpha
                    mask = mask >>> 1;
                }
                offb++;
            }
        }

        return _encodePNG(width, height, image, level);
    }

    /**
     * デフォルトの圧縮レベルでDIB (Device Independent Bitmaps) を
     * PNG (Portable Network Graphics) に変換します。
     *
     * @param b DIBデータ
     * @return PNGデータ
     */
    public static byte[] dibToPNG(byte[] b) {
        return dibToPNG(b, Deflater.DEFAULT_COMPRESSION);
    }

    /**
     * 指定された圧縮レベルでDIB (Device Independent Bitmaps) を
     * PNG (Portable Network Graphics) に変換します。
     *
     * @param b DIBデータ
     * @param level 圧縮レベル (0-9)
     * @return PNGデータ
     */
    public static byte[] dibToPNG(byte[] b, int level) {
        if ((b[0] & 0xff) != 'B' || (b[1] & 0xff) != 'M') {
            _logger.warn("file type 'BM' is not found in file header");
            return new byte[0];
        }

        int off = (int) ByteUtil.getLongLE4(b, 10);
        int hsize = (int)ByteUtil.getLongLE4(b, 14);
        int width = 0;
        int height = 0;
        int bitCount = 0;
        int compress = 0;
        int paletteOff = 14 + hsize;
        int paletteSize = 0;
        int paletteCnt = 0;
        int[] bitField = new int[4];
        Arrays.fill(bitField, 0);
        if (hsize < 40) {
            // OS/2 Bitmap
            width = ByteUtil.getIntLE2(b, 18);
            height = ByteUtil.getIntLE2(b, 20);
            bitCount = ByteUtil.getIntLE2(b, 24);
            paletteSize = 3;
        } else {
            // Windows Bitmap
            width = (int)ByteUtil.getLongLE4(b, 18);
            height = (int)ByteUtil.getLongLE4(b, 22);
            bitCount = ByteUtil.getIntLE2(b, 28);
            compress = (int)ByteUtil.getLongLE4(b, 30);
            paletteCnt = (int)ByteUtil.getLongLE4(b, 46);
            paletteSize = 4;
            if (hsize < 52) {
                if (compress == 0) {
                    if (bitCount == 16) {
                        bitField[0] = 0x00007c00;
                        bitField[1] = 0x000003e0;
                        bitField[2] = 0x0000001f;
                    } else if (bitCount == 32) {
                        bitField[0] = 0x00ff0000;
                        bitField[1] = 0x0000ff00;
                        bitField[2] = 0x000000ff;
                    }
                } else if (compress == 3) {
                    if (bitCount == 16 || bitCount == 32) {
                        bitField[0] = (int)ByteUtil.getLongLE4(b, 54);
                        bitField[1] = (int)ByteUtil.getLongLE4(b, 58);
                        bitField[2] = (int)ByteUtil.getLongLE4(b, 62);
                        // カラーパレットはビットフィールドの後
                        paletteOff += 12;
                    }
                }
            } else {
                bitField[0] = (int)ByteUtil.getLongLE4(b, 54);
                bitField[1] = (int)ByteUtil.getLongLE4(b, 58);
                bitField[2] = (int)ByteUtil.getLongLE4(b, 62);
                bitField[3] = (int)ByteUtil.getLongLE4(b, 66);
            }
        }

        int n = bitField.length;
        int[] bitNum = new int[n];
        Arrays.fill(bitNum, 0);
        int[] bitShift = new int[n];
        Arrays.fill(bitShift, 0);
        for (int i=0; i<n; i++) {
            int mask = bitField[i];
            if (mask != 0) {
                // ビットシフト数
                while ((mask&0x01) == 0) {
                    mask = mask >>> 1;
                    bitShift[i]++;
                }
                // ビット数
                int cnt = bitShift[i];
                while ((mask&0x01) != 0) {
                    mask = mask >>> 1;
                    bitNum[i]++;
                    cnt++;
                    if (cnt >= bitCount) {
                        break;
                    }
                }
                // ビット数からビット数で表される最大値に変換
                cnt = 0x01;
                for (int j=0; j<bitNum[i]; j++) {
                    cnt = cnt << 1;
                }
                bitNum[i] = cnt - 1;
            }
        }

        if (off == 0) {
            off = 14 + hsize;
            if (paletteCnt != 0) {
                off += paletteSize * paletteCnt;
            } else {
                if (bitCount == 1 || bitCount == 4 || bitCount == 8) {
                    off += paletteSize * (0x01 << bitCount);
                }
            }
            if (hsize == 40) {
                if (compress == 3 && (bitCount == 16 || bitCount == 32)) {
                    off += 12;
                }
            }
        }

        // RLEの伸張
        byte[] dib = b;
        if (compress > 0) {
            if (compress == 1 && bitCount == 8) {
                // 8bitランレングス圧縮
                dib = _expandRLE8(b, off, width, height);
            } else if (compress == 2 && bitCount == 4) {
                // 4bitランレングス圧縮
                dib = _expandRLE4(b, off, width, height);
            } else if (compress == 3 && (bitCount == 16 || bitCount == 32)) {
                // ビットフィールド
            } else {
                _logger.warn("unknown compression type:"
                             + " compress=" + compress
                             + " bitCount=" + bitCount);
                return new byte[0];
            }
        }

        // 1行のバイト数の算出 (4byte境界に揃える)
        int lineBytes = (width * bitCount + 31) / 32 * 4;
        byte[] line = new byte[lineBytes];

        // イメージデータの作成
        byte[] image = new byte[(width*4+1)*height];
        Arrays.fill(image, (byte)0x00);
        int idx = 0;
        switch (bitCount) {
            case 1: {
                for (int y=height-1; y>=0; y--) {
                    image[idx++] = 0x00; // filter type
                    System.arraycopy(dib, off+lineBytes*y, line, 0, lineBytes);
                    for (int x=0; x<width; x+=8) {
                        int cnt = 8;
                        if (x+8 > width) {
                            cnt = width - x;
                        }
                        int mask = 0x80;
                        for (int i=0; i<cnt; i++) {
                            int palette = paletteOff;
                            if ((line[x/8] & mask) > 0) {
                                palette += paletteSize;
                            }
                            image[idx++] = dib[palette+2]; // R
                            image[idx++] = dib[palette+1]; // G
                            image[idx++] = dib[palette];   // B
                            image[idx++] = (byte)0xff;     // alpha
                            mask = mask >>> 1;
                        }
                    }
                }
                break;
            }
            case 4: {
                for (int y=height-1; y>=0; y--) {
                    image[idx++] = 0x00; // filter type
                    System.arraycopy(dib, off+lineBytes*y, line, 0, lineBytes);
                    for (int x=0; x<width; x+=2) {
                        int palette = paletteOff + ((line[x/2] >>> 4) & 0x0f) * paletteSize;
                        image[idx++] = dib[palette+2]; // R
                        image[idx++] = dib[palette+1]; // G
                        image[idx++] = dib[palette];   // B
                        image[idx++] = (byte)0xff;     // alpha
                        if (width - x > 1) {
                            palette = paletteOff + (line[x/2] & 0x0f) * paletteSize;
                            image[idx++] = dib[palette+2]; // R
                            image[idx++] = dib[palette+1]; // G
                            image[idx++] = dib[palette];   // B
                            image[idx++] = (byte)0xff;     // alpha
                        }
                    }
                }
                break;
            }
            case 8: {
                for (int y=height-1; y>=0; y--) {
                    image[idx++] = 0x00; // filter type
                    System.arraycopy(dib, off+lineBytes*y, line, 0, lineBytes);
                    for (int x=0; x<width; x++) {
                        int palette = paletteOff + (line[x] & 0xff) * paletteSize;
                        image[idx++] = dib[palette+2]; // R
                        image[idx++] = dib[palette+1]; // G
                        image[idx++] = dib[palette];   // B
                        image[idx++] = (byte)0xff;     // alpha
                    }
                }
                break;
            }
            case 16: {
                for (int y=height-1; y>=0; y--) {
                    image[idx++] = 0x00; // filter type
                    System.arraycopy(dib, off+lineBytes*y, line, 0, lineBytes);
                    int w = width * 4;
                    for (int x=0; x<w; x+=2) {
                        int val = ByteUtil.getIntLE2(line, x);
                        // RGB
                        for (int i=0; i<3; i++) {
                            int pxl = (val & bitField[i]) >>> bitShift[i];
                            if (bitNum[i] != 255) {
                                pxl = Math.round(pxl*255.0f/bitNum[i]);
                            }
                            image[idx++] = (byte)pxl;
                        }
                        // alpha
                        if (bitField[3] == 0) {
                            image[idx++] = (byte)0xff;
                        } else {
                            int pxl = (val & bitField[3]) >>> bitShift[3];
                            if (bitNum[3] != 255) {
                                pxl = Math.round(pxl*255.0f/bitNum[3]);
                            }
                            image[idx++] = (byte)pxl;
                        }
                    }
                }
                break;
            }
            case 24: {
                for (int y=height-1; y>=0; y--) {
                    image[idx++] = 0x00; // filter type
                    System.arraycopy(dib, off+lineBytes*y, line, 0, lineBytes);
                    int w = width * 3;
                    for (int x=0; x<w; x+=3) {
                        image[idx++] = line[x+2];  // R
                        image[idx++] = line[x+1];  // G
                        image[idx++] = line[x];    // B
                        image[idx++] = (byte)0xff; // alpha
                    }
                }
                break;
            }
            case 32: {
                for (int y=height-1; y>=0; y--) {
                    image[idx++] = 0x00; // filter type
                    System.arraycopy(dib, off+lineBytes*y, line, 0, lineBytes);
                    int w = width * 4;
                    for (int x=0; x<w; x+=4) {
                        int val = (int)ByteUtil.getLongLE4(line, x);
                        // RGB
                        for (int i=0; i<3; i++) {
                            int pxl = (val & bitField[i]) >>> bitShift[i];
                            if (bitNum[i] != 255) {
                                pxl = Math.round(pxl*255.0f/bitNum[i]);
                            }
                            image[idx++] = (byte)pxl;
                        }
                        // alpha
                        if (bitField[3] == 0) {
                            image[idx++] = (byte)0xff;
                        } else {
                            val = ~val;
                            int pxl = (val & bitField[3]) >>> bitShift[3];
                            if (bitNum[3] != 255) {
                                pxl = Math.round(pxl*255.0f/bitNum[3]);
                            }
                            image[idx++] = (byte)pxl;
                        }
                    }
                }
                break;
            }
            default:
                _logger.warn("unknown bit count: " + bitCount);
                return new byte[0];
        }

        return _encodePNG(width, height, image, level);
    }

    /**
     * 8bitランレングスエンコードされたDIBデータを伸張します。
     *
     * @param rle RLE-DIBデータ
     * @param off イメージデータの位置
     * @param width 幅
     * @param height 高さ
     * @return 伸張したDIBデータ (ヘッダはそのまま)
     */
    private static byte[] _expandRLE8(byte[] rle, int off, int width, int height) {
        // 1行のバイト数の算出
        int lineBytes = (width * 8 + 31) / 32 * 4;
        // 展開後のサイズ
        int size = off + lineBytes * height;
        byte[] dib = new byte[size];
        Arrays.fill(dib, (byte)0x00);
        System.arraycopy(rle, 0, dib, 0, off); // ヘッダはそのままコピー

        int sidx = off;
        for (int y=0; y<height; y++) {
            int didx = off + lineBytes * y;
            int x = 0;
            while (x < width) {
                int code1 = rle[sidx++] & 0xff; // インデックス数
                int code2 = rle[sidx++] & 0xff; // インデックス
                // エスケープコード
                if (code1 == 0x00) {
                    boolean eol = false;
                    switch (code2) {
                        case 0x00: // end of line
                            if (x > 0) {
                                eol = true;
                            }
                            break;
                        case 0x01: // end of block
                            return dib;
                        case 0x02: // skip
                            code1 = rle[sidx++] & 0xff;
                            code2 = rle[sidx++] & 0xff;
                            x += code1;
                            y += code2;
                            didx += code1 + lineBytes * code2;
                            break;
                        default: // absolute mode
                            x += code2;
                            for (int i=0; i<code2; i++) {
                                dib[didx++] = rle[sidx++];
                            }
                            if ((code2%2) != 0) {
                                sidx++;
                            }
                            break;
                    }
                    if (eol) {
                        break;
                    }
                } else {
                    x += code1;
                    for (int i=0; i<code1; i++) {
                        dib[didx++] = (byte)code2;
                    }
                }
            }
        }
        return dib;
    }

    /**
     * 4bitランレングスエンコードされたDIBデータを伸張します。
     *
     * @param rle RLE-DIBデータ
     * @param off イメージデータの位置
     * @param width 幅
     * @param height 高さ
     * @return 伸張したDIBデータ (ヘッダはそのまま)
     */
    private static byte[] _expandRLE4(byte[] rle, int off, int width, int height) {
        // 1行のバイト数の算出
        int lineBytes = (width * 4 + 31) / 32 * 4;
        // 展開後のサイズ
        int size = off + lineBytes * height;
        byte[] dib = new byte[size];
        Arrays.fill(dib, (byte)0x00);
        System.arraycopy(rle, 0, dib, 0, off); // ヘッダはそのままコピー

        int sidx = off;
        for (int y=0; y<height; y++) {
            int didx = off + lineBytes * y;
            boolean high = true;
            int x = 0;
            while (x < width) {
                int code1 = rle[sidx++] & 0xff; // インデックス数
                int code2 = rle[sidx++] & 0xff; // インデックス
                // エスケープコード
                if (code1 == 0x00) {
                    boolean eol = false;
                    switch (code2) {
                        case 0x00: // end of line
                            if (x > 0) {
                                eol = true;
                            }
                            break;
                        case 0x01: // end of block
                            return dib;
                        case 0x02: { // skip
                            code1 = rle[sidx++] & 0xff;
                            code2 = rle[sidx++] & 0xff;
                            x += code1;
                            y += code2;
                            didx += code1 / 2 + lineBytes * code2;
                            if ((code1%2) != 0) {
                                high = !high;
                                if (high) {
                                    didx++;
                                }
                            }
                            break;
                        }
                        default: // absolute mode
                            x += code2;
                            int cnt = (code2 + 1) / 2;
                            if (high) {
                                for (int i=0; i<cnt; i++) {
                                    dib[didx++] = rle[sidx++];
                                }
                                if ((code2%2) != 0) {
                                    didx--;
                                    dib[didx] &= 0xf0;
                                    high = false;
                                }
                            } else {
                                for (int i=0; i<cnt; i++) {
                                    dib[didx++] |= (rle[sidx] >>> 4) & 0x0f;
                                    dib[didx] |= (rle[sidx++] << 4) & 0xf0;
                                }
                                if ((code2%2) != 0) {
                                    dib[didx] = (byte)0x00;
                                    high = true;
                                }
                            }
                            if ((cnt%2) != 0) {
                                sidx++;
                            }
                            break;
                    }
                    if (eol) {
                        break;
                    }
                } else {
                    x += code1;
                    if (!high) {
                        dib[didx++] = (byte)((code2 >>> 4) & 0x0f);
                        code2 = ((code2 >>> 4) & 0x0f) | ((code2 << 4) & 0xf0);
                        code1--;
                        high = true;
                    }
                    int cnt = (code1 + 1) / 2;
                    for (int i=0; i<cnt; i++) {
                        dib[didx++] = (byte)code2;
                    }
                    if ((code1%2) != 0) {
                        didx--;
                        dib[didx] &= 0xf0;
                        high = false;
                    }
                }
            }
        }
        return dib;
    }

    /**
     * イメージデータをPNGにエンコードします。
     *
     * @param width 幅
     * @param height 高さ
     * @param image イメージデータ
     * @param level 圧縮レベル
     * @return PNGデータ
     */
    private static byte[] _encodePNG(int width, int height,
                                     byte[] image, int level) {
        // イメージデータの圧縮
        byte[] buf = new byte[image.length+62];
        Deflater def = new Deflater(level);
        def.setInput(image, 0, image.length);
        def.finish();
        int len = 0;
        while (!def.needsInput()) {
            int n = def.deflate(buf, len, buf.length-len);
            len += n;
        }
        def.end();

        // PNGデータの作成
        int size = PNG_HEADER.length + len + 12 + PNG_FOOTER.length;
        byte[] png = new byte[size];
        Arrays.fill(png, (byte)0x00);

        // ファイルシグネチャ、IHDRブロック
        System.arraycopy(PNG_HEADER, 0, png, 0, PNG_HEADER.length);

        // イメージの幅
        png[16] = (byte)((width >>> 24) & 0xff);
        png[17] = (byte)((width >>> 16) & 0xff);
        png[18] = (byte)((width >>> 8) & 0xff);
        png[19] = (byte)(width & 0xff);
        // イメージの高さ
        png[20] = (byte)((height >>> 24) & 0xff);
        png[21] = (byte)((height >>> 16) & 0xff);
        png[22] = (byte)((height >>> 8) & 0xff);
        png[23] = (byte)(height & 0xff);
        // IHDRブロックのCRC
        CRC32 crc = new CRC32();
        crc.update(png, 12, 17);
        long c = crc.getValue();
        png[29] = (byte)((c >>> 24) & 0xff);
        png[30] = (byte)((c >>> 16) & 0xff);
        png[31] = (byte)((c >>> 8) & 0xff);
        png[32] = (byte)(c & 0xff);

        // IDATブロック
        int off = PNG_HEADER.length;
        // データ長
        png[off++] = (byte)((len >>> 24) & 0xff);
        png[off++] = (byte)((len >>> 16) & 0xff);
        png[off++] = (byte)((len >>> 8) & 0xff);
        png[off++] = (byte)(len & 0xff);
        // ブロックタイプ (イメージデータ)
        png[off++] = 'I';
        png[off++] = 'D';
        png[off++] = 'A';
        png[off++] = 'T';
        // 圧縮したイメージデータ
        System.arraycopy(buf, 0, png, off, len);
        // IDATブロックのCRC
        crc.reset();
        crc.update(png, off-4, len+4);
        c = crc.getValue();
        off += len;
        png[off++] = (byte)((c >>> 24) & 0xff);
        png[off++] = (byte)((c >>> 16) & 0xff);
        png[off++] = (byte)((c >>> 8) & 0xff);
        png[off++] = (byte)(c & 0xff);

        // IENDブロック
        System.arraycopy(PNG_FOOTER, 0, png, off, PNG_FOOTER.length);

        return png;
    }
}

// end of ImageUtil.java