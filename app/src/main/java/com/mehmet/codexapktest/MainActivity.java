package com.mehmet.codexapktest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setStatusBarColor(Color.rgb(247, 126, 174));
        getWindow().setNavigationBarColor(Color.rgb(90, 47, 127));
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
            }
        }
        setContentView(new CandyGameView());
    }

    private final class CandyGameView extends View {
        private static final int SIZE = 8;
        private static final int COLORS = 6;
        private static final int MOVES_TOTAL = 24;
        private static final int TARGET = 6000;

        private final int[][] board = new int[SIZE][SIZE];
        private final int[][] special = new int[SIZE][SIZE];
        private final Random random = new Random();
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();
        private Bitmap background;
        private float boardLeft;
        private float boardTop;
        private float cell;
        private float downX;
        private float downY;
        private int selectedRow = -1;
        private int selectedCol = -1;
        private int moves = MOVES_TOTAL;
        private int score = 0;
        private int best = 0;
        private String message = "Bir sekeri kaydir ve eslestir!";
        private boolean gameOver = false;
        private boolean won = false;

        private final int[] colors = {
                Color.rgb(246, 70, 115),
                Color.rgb(39, 190, 217),
                Color.rgb(255, 190, 42),
                Color.rgb(143, 84, 225),
                Color.rgb(85, 201, 100),
                Color.rgb(255, 122, 55)
        };

        CandyGameView() {
            super(MainActivity.this);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            text.setTypeface(android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD));
            background = loadBackground();
            newGame();
        }

        private Bitmap loadBackground() {
            try {
                InputStream in = getResources().openRawResource(
                        getResources().getIdentifier("candy_bg_b64", "raw", getPackageName()));
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int count;
                while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
                in.close();
                byte[] decoded = Base64.decode(out.toByteArray(), Base64.DEFAULT);
                return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            } catch (Exception ignored) {
                return null;
            }
        }

        private void newGame() {
            score = 0;
            moves = MOVES_TOTAL;
            gameOver = false;
            won = false;
            message = "Bir sekeri kaydir ve eslestir!";
            do {
                for (int r = 0; r < SIZE; r++) {
                    for (int c = 0; c < SIZE; c++) {
                        int value;
                        do {
                            value = random.nextInt(COLORS);
                        } while ((c >= 2 && board[r][c - 1] == value && board[r][c - 2] == value)
                                || (r >= 2 && board[r - 1][c] == value && board[r - 2][c] == value));
                        board[r][c] = value;
                        special[r][c] = 0;
                    }
                }
            } while (!hasPossibleMove());
            selectedRow = selectedCol = -1;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            drawBackground(canvas);
            float width = getWidth();
            float topSafe = dp(18);

            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(Color.WHITE);
            text.setShadowLayer(dp(4), 0, dp(2), 0x66000000);
            text.setTextSize(sp(30));
            canvas.drawText("SEKER ALEMI", width / 2f, topSafe + dp(34), text);

            drawPill(canvas, dp(16), topSafe + dp(52), width / 3f - dp(8), dp(64),
                    "SKOR", String.valueOf(score), 0xE6F55087);
            drawPill(canvas, width / 3f + dp(4), topSafe + dp(52),
                    width / 3f - dp(8), dp(64), "HEDEF", String.valueOf(TARGET), 0xE64B9ED8);
            drawPill(canvas, width * 2f / 3f + dp(4), topSafe + dp(52),
                    width / 3f - dp(20), dp(64), "HAMLE", String.valueOf(moves), 0xE68655C9);

            boardLeft = dp(12);
            cell = (width - dp(24)) / SIZE;
            boardTop = topSafe + dp(130);
            float boardSize = cell * SIZE;

            paint.setColor(0xCCFFFFFF);
            paint.setShadowLayer(dp(10), 0, dp(5), 0x55000000);
            rect.set(boardLeft - dp(5), boardTop - dp(5),
                    boardLeft + boardSize + dp(5), boardTop + boardSize + dp(5));
            canvas.drawRoundRect(rect, dp(18), dp(18), paint);
            paint.clearShadowLayer();

            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    float cx = boardLeft + c * cell + cell / 2f;
                    float cy = boardTop + r * cell + cell / 2f;
                    paint.setColor(((r + c) & 1) == 0 ? 0x35FFFFFF : 0x20A96CCB);
                    rect.set(cx - cell * .46f, cy - cell * .46f,
                            cx + cell * .46f, cy + cell * .46f);
                    canvas.drawRoundRect(rect, cell * .16f, cell * .16f, paint);
                    drawCandy(canvas, cx, cy, cell * .36f, board[r][c], special[r][c]);
                }
            }

            if (selectedRow >= 0) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(dp(3));
                paint.setColor(Color.WHITE);
                paint.setShadowLayer(dp(6), 0, 0, 0xAAE83E8C);
                float l = boardLeft + selectedCol * cell + dp(2);
                float t = boardTop + selectedRow * cell + dp(2);
                rect.set(l, t, l + cell - dp(4), t + cell - dp(4));
                canvas.drawRoundRect(rect, dp(13), dp(13), paint);
                paint.setStyle(Paint.Style.FILL);
                paint.clearShadowLayer();
            }

            float infoTop = boardTop + boardSize + dp(14);
            paint.setColor(0xEFFFFFFF);
            rect.set(dp(16), infoTop, width - dp(16), infoTop + dp(58));
            canvas.drawRoundRect(rect, dp(18), dp(18), paint);
            text.clearShadowLayer();
            text.setTextSize(sp(15));
            text.setColor(0xFF753A83);
            canvas.drawText(message, width / 2f, infoTop + dp(25), text);

            float progress = Math.min(1f, score / (float) TARGET);
            paint.setColor(0x1F7A3B83);
            rect.set(dp(36), infoTop + dp(36), width - dp(36), infoTop + dp(45));
            canvas.drawRoundRect(rect, dp(6), dp(6), paint);
            paint.setShader(new LinearGradient(dp(36), 0, width - dp(36), 0,
                    0xFFFF5B8E, 0xFF7655D8, Shader.TileMode.CLAMP));
            rect.right = dp(36) + (width - dp(72)) * progress;
            canvas.drawRoundRect(rect, dp(6), dp(6), paint);
            paint.setShader(null);

            if (gameOver) drawGameOver(canvas);
        }

        private void drawBackground(Canvas canvas) {
            if (background != null) {
                float scale = Math.max(getWidth() / (float) background.getWidth(),
                        getHeight() / (float) background.getHeight());
                float sw = background.getWidth() * scale;
                float sh = background.getHeight() * scale;
                rect.set((getWidth() - sw) / 2f, (getHeight() - sh) / 2f,
                        (getWidth() + sw) / 2f, (getHeight() + sh) / 2f);
                paint.setAlpha(255);
                canvas.drawBitmap(background, null, rect, paint);
            } else {
                canvas.drawColor(0xFFFFD8E8);
            }
            paint.setColor(0x18FFFFFF);
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        }

        private void drawPill(Canvas canvas, float x, float y, float w, float h,
                              String label, String value, int color) {
            paint.setColor(color);
            paint.setShadowLayer(dp(6), 0, dp(3), 0x44000000);
            rect.set(x, y, x + w, y + h);
            canvas.drawRoundRect(rect, dp(18), dp(18), paint);
            paint.clearShadowLayer();
            text.clearShadowLayer();
            text.setColor(0xEFFFFFFF);
            text.setTextSize(sp(10));
            canvas.drawText(label, x + w / 2f, y + dp(20), text);
            text.setColor(Color.WHITE);
            text.setTextSize(sp(21));
            canvas.drawText(value, x + w / 2f, y + dp(48), text);
        }

        private void drawCandy(Canvas canvas, float cx, float cy, float radius,
                               int type, int specialType) {
            if (type == 6) {
                paint.setShader(new LinearGradient(cx - radius, cy - radius,
                        cx + radius, cy + radius,
                        new int[]{0xFF43235E, 0xFF1D183A, 0xFF7A3B83}, null,
                        Shader.TileMode.CLAMP));
                paint.setShadowLayer(dp(4), 0, dp(3), 0x66000000);
                canvas.drawCircle(cx, cy, radius, paint);
                paint.clearShadowLayer();
                paint.setShader(null);
                for (int i = 0; i < 8; i++) {
                    double a = i * Math.PI / 4;
                    paint.setColor(colors[i % COLORS]);
                    canvas.drawCircle(cx + (float) Math.cos(a) * radius * .55f,
                            cy + (float) Math.sin(a) * radius * .55f,
                            radius * .12f, paint);
                }
                paint.setColor(Color.WHITE);
                canvas.drawCircle(cx - radius * .3f, cy - radius * .35f, radius * .13f, paint);
                return;
            }

            int base = colors[type];
            paint.setColor(base);
            paint.setShadowLayer(dp(4), 0, dp(3), 0x66000000);
            Path p = new Path();
            switch (type) {
                case 0:
                    p.moveTo(cx, cy - radius);
                    p.cubicTo(cx + radius * 1.15f, cy - radius,
                            cx + radius, cy + radius * .8f, cx, cy + radius);
                    p.cubicTo(cx - radius, cy + radius * .8f,
                            cx - radius * 1.15f, cy - radius, cx, cy - radius);
                    canvas.drawPath(p, paint);
                    break;
                case 1:
                    rect.set(cx - radius, cy - radius * .8f, cx + radius, cy + radius * .8f);
                    canvas.drawRoundRect(rect, radius * .35f, radius * .35f, paint);
                    break;
                case 2:
                    p.moveTo(cx, cy - radius);
                    p.lineTo(cx + radius * .92f, cy - radius * .25f);
                    p.lineTo(cx + radius * .58f, cy + radius);
                    p.lineTo(cx - radius * .58f, cy + radius);
                    p.lineTo(cx - radius * .92f, cy - radius * .25f);
                    p.close();
                    canvas.drawPath(p, paint);
                    break;
                case 3:
                    canvas.drawCircle(cx, cy, radius, paint);
                    break;
                case 4:
                    rect.set(cx - radius * .9f, cy - radius * .9f,
                            cx + radius * .9f, cy + radius * .9f);
                    canvas.drawRoundRect(rect, radius * .48f, radius * .48f, paint);
                    break;
                default:
                    p.moveTo(cx, cy - radius);
                    p.lineTo(cx + radius, cy);
                    p.lineTo(cx, cy + radius);
                    p.lineTo(cx - radius, cy);
                    p.close();
                    canvas.drawPath(p, paint);
            }
            paint.clearShadowLayer();

            paint.setColor(0x66FFFFFF);
            canvas.drawOval(cx - radius * .5f, cy - radius * .62f,
                    cx + radius * .15f, cy - radius * .24f, paint);
            if (specialType == 1) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(radius * .15f);
                paint.setColor(0xDDFFFFFF);
                for (int i = -1; i <= 1; i++) {
                    canvas.drawLine(cx - radius * .72f, cy + i * radius * .35f,
                            cx + radius * .72f, cy + i * radius * .35f, paint);
                }
                paint.setStyle(Paint.Style.FILL);
            }
        }

        private void drawGameOver(Canvas canvas) {
            paint.setColor(0xAA3B1D54);
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
            float w = getWidth() - dp(52);
            float h = dp(250);
            float x = dp(26);
            float y = (getHeight() - h) / 2f;
            paint.setColor(0xFFFDF7FF);
            paint.setShadowLayer(dp(18), 0, dp(8), 0x66000000);
            rect.set(x, y, x + w, y + h);
            canvas.drawRoundRect(rect, dp(26), dp(26), paint);
            paint.clearShadowLayer();

            text.setColor(won ? 0xFFF04478 : 0xFF7655C9);
            text.setTextSize(sp(30));
            canvas.drawText(won ? "HARIKA!" : "HAMLE BITTI", getWidth() / 2f, y + dp(55), text);
            text.setColor(0xFF67416F);
            text.setTextSize(sp(17));
            canvas.drawText("Skorun: " + score, getWidth() / 2f, y + dp(94), text);
            text.setTextSize(sp(14));
            canvas.drawText(won ? "Seker ustasi oldun!" : "Bir kez daha dene!",
                    getWidth() / 2f, y + dp(122), text);

            paint.setShader(new LinearGradient(x + dp(34), y, x + w - dp(34), y,
                    0xFFFF4F86, 0xFF8258D5, Shader.TileMode.CLAMP));
            rect.set(x + dp(34), y + dp(150), x + w - dp(34), y + dp(210));
            canvas.drawRoundRect(rect, dp(22), dp(22), paint);
            paint.setShader(null);
            text.setColor(Color.WHITE);
            text.setTextSize(sp(17));
            canvas.drawText("YENIDEN OYNA", getWidth() / 2f, y + dp(188), text);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                downX = event.getX();
                downY = event.getY();
                return true;
            }
            if (event.getAction() != MotionEvent.ACTION_UP) return true;

            if (gameOver) {
                float y = (getHeight() - dp(250)) / 2f;
                if (event.getY() > y + dp(140) && event.getY() < y + dp(225)) newGame();
                return true;
            }

            int c = (int) ((downX - boardLeft) / cell);
            int r = (int) ((downY - boardTop) / cell);
            if (r < 0 || r >= SIZE || c < 0 || c >= SIZE) return true;

            float dx = event.getX() - downX;
            float dy = event.getY() - downY;
            int nr = r;
            int nc = c;
            if (Math.max(Math.abs(dx), Math.abs(dy)) > cell * .25f) {
                if (Math.abs(dx) > Math.abs(dy)) nc += dx > 0 ? 1 : -1;
                else nr += dy > 0 ? 1 : -1;
                if (inside(nr, nc)) trySwap(r, c, nr, nc);
                return true;
            }

            if (selectedRow >= 0 && Math.abs(selectedRow - r) + Math.abs(selectedCol - c) == 1) {
                trySwap(selectedRow, selectedCol, r, c);
                selectedRow = selectedCol = -1;
            } else {
                selectedRow = r;
                selectedCol = c;
                message = "Komsu bir sekeri sec veya kaydir.";
                invalidate();
            }
            return true;
        }

        private void trySwap(int r1, int c1, int r2, int c2) {
            int previousScore = score;
            if (!inside(r2, c2)) return;
            int firstType = board[r1][c1];
            int secondType = board[r2][c2];
            int firstSpecial = special[r1][c1];
            int secondSpecial = special[r2][c2];
            swap(r1, c1, r2, c2);

            boolean specialMove = firstType == 6 || secondType == 6 || firstSpecial == 1 || secondSpecial == 1;
            if (!specialMove && !hasMatches()) {
                swap(r1, c1, r2, c2);
                message = "Bu hamlede eslesme yok.";
                invalidate();
                return;
            }

            moves--;
            int gained = 0;
            if (firstType == 6 || secondType == 6) {
                int targetColor = firstType == 6 ? secondType : firstType;
                gained += clearColor(targetColor);
            }
            if (firstSpecial == 1) gained += clearCross(r2, c2);
            if (secondSpecial == 1) gained += clearCross(r1, c1);
            if (gained > 0) {
                score += gained * 80;
                collapse();
            }
            int chain = resolveMatches();
            best = Math.max(best, score);
            message = chain > 1 ? "Zincirleme patlama x" + chain + "!"
                    : "Guzel hamle! +" + Math.max(0, score - previousScore);
            if (score >= TARGET) {
                won = true;
                gameOver = true;
                message = "Hedef tamamlandi!";
            } else if (moves <= 0) {
                gameOver = true;
            } else if (!hasPossibleMove()) {
                shuffle();
                message = "Tahta karistirildi!";
            }
            invalidate();
        }

        private int resolveMatches() {
            int chain = 0;
            while (chain < 12) {
                MatchResult result = findMatches();
                if (result.count == 0) break;
                chain++;
                score += result.count * 70 * chain;
                for (int r = 0; r < SIZE; r++) {
                    for (int c = 0; c < SIZE; c++) {
                        if (result.mark[r][c]) {
                            board[r][c] = -1;
                            special[r][c] = 0;
                        }
                    }
                }
                for (SpecialDrop drop : result.drops) {
                    board[drop.row][drop.col] = drop.color;
                    special[drop.row][drop.col] = drop.kind;
                }
                collapse();
            }
            return chain;
        }

        private MatchResult findMatches() {
            MatchResult result = new MatchResult();
            for (int r = 0; r < SIZE; r++) {
                int start = 0;
                while (start < SIZE) {
                    int color = board[r][start];
                    int end = start + 1;
                    while (end < SIZE && color >= 0 && color < COLORS && board[r][end] == color) end++;
                    int len = end - start;
                    if (len >= 3) {
                        for (int c = start; c < end; c++) result.mark[r][c] = true;
                        result.count += len;
                        if (len >= 4) result.drops.add(new SpecialDrop(r, start + len / 2,
                                len >= 5 ? 6 : color, len >= 5 ? 0 : 1));
                    }
                    start = end;
                }
            }
            for (int c = 0; c < SIZE; c++) {
                int start = 0;
                while (start < SIZE) {
                    int color = board[start][c];
                    int end = start + 1;
                    while (end < SIZE && color >= 0 && color < COLORS && board[end][c] == color) end++;
                    int len = end - start;
                    if (len >= 3) {
                        for (int r = start; r < end; r++) result.mark[r][c] = true;
                        result.count += len;
                        if (len >= 4) result.drops.add(new SpecialDrop(start + len / 2, c,
                                len >= 5 ? 6 : color, len >= 5 ? 0 : 1));
                    }
                    start = end;
                }
            }
            return result;
        }

        private boolean hasMatches() {
            return findMatches().count > 0;
        }

        private void collapse() {
            for (int c = 0; c < SIZE; c++) {
                int write = SIZE - 1;
                for (int r = SIZE - 1; r >= 0; r--) {
                    if (board[r][c] >= 0) {
                        board[write][c] = board[r][c];
                        special[write][c] = special[r][c];
                        write--;
                    }
                }
                while (write >= 0) {
                    board[write][c] = random.nextInt(COLORS);
                    special[write][c] = 0;
                    write--;
                }
            }
        }

        private int clearColor(int color) {
            int count = 0;
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    if (board[r][c] == color || board[r][c] == 6) {
                        board[r][c] = -1;
                        special[r][c] = 0;
                        count++;
                    }
                }
            }
            return count;
        }

        private int clearCross(int row, int col) {
            int count = 0;
            for (int i = 0; i < SIZE; i++) {
                if (board[row][i] >= 0) count++;
                board[row][i] = -1;
                special[row][i] = 0;
                if (i != row && board[i][col] >= 0) count++;
                board[i][col] = -1;
                special[i][col] = 0;
            }
            return count;
        }

        private boolean hasPossibleMove() {
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    if (c + 1 < SIZE && createsMatch(r, c, r, c + 1)) return true;
                    if (r + 1 < SIZE && createsMatch(r, c, r + 1, c)) return true;
                }
            }
            return false;
        }

        private boolean createsMatch(int r1, int c1, int r2, int c2) {
            swap(r1, c1, r2, c2);
            boolean result = hasMatches();
            swap(r1, c1, r2, c2);
            return result;
        }

        private void shuffle() {
            List<Integer> values = new ArrayList<>();
            for (int[] row : board) for (int value : row) values.add(value < COLORS ? value : random.nextInt(COLORS));
            do {
                java.util.Collections.shuffle(values);
                int i = 0;
                for (int r = 0; r < SIZE; r++) {
                    for (int c = 0; c < SIZE; c++) {
                        board[r][c] = values.get(i++);
                        special[r][c] = 0;
                    }
                }
            } while (hasMatches() || !hasPossibleMove());
        }

        private void swap(int r1, int c1, int r2, int c2) {
            int t = board[r1][c1];
            board[r1][c1] = board[r2][c2];
            board[r2][c2] = t;
            t = special[r1][c1];
            special[r1][c1] = special[r2][c2];
            special[r2][c2] = t;
        }

        private boolean inside(int r, int c) {
            return r >= 0 && r < SIZE && c >= 0 && c < SIZE;
        }

        private float dp(float value) {
            return value * getResources().getDisplayMetrics().density;
        }

        private float sp(float value) {
            return value * getResources().getDisplayMetrics().scaledDensity;
        }

        private final class MatchResult {
            final boolean[][] mark = new boolean[SIZE][SIZE];
            final List<SpecialDrop> drops = new ArrayList<>();
            int count;
        }

        private final class SpecialDrop {
            final int row;
            final int col;
            final int color;
            final int kind;

            SpecialDrop(int row, int col, int color, int kind) {
                this.row = row;
                this.col = col;
                this.color = color;
                this.kind = kind;
            }
        }
    }
}
