package com.example.sunghun.hw_4b_0602;

        import android.app.ActionBar.LayoutParams;
        import android.app.Activity;
        import android.content.Context;
        import android.graphics.Bitmap;
        import android.graphics.Canvas;
        import android.graphics.Color;
        import android.graphics.Paint;
        import android.graphics.PixelFormat;
        import android.graphics.Rect;
        import android.graphics.drawable.BitmapDrawable;

        import android.os.Bundle;

        import android.util.AttributeSet;

        import android.view.Display;
        import android.view.MotionEvent;
        import android.view.SurfaceHolder;
        import android.view.SurfaceView;
        import android.view.ViewManager;
        import android.view.Window;
        import android.view.WindowManager;

        import android.widget.ImageView;
        import android.widget.TableLayout;
        import android.widget.TableRow;
        import android.widget.Toast;

        import java.util.ArrayList;

public class Hw4B extends Activity {
    int i, j;
    int[][] maze;
    int[] imageXY = new int[2];
    boolean start, end, collision;

    Bitmap block, point_s, point_e, road;
    DrawingSurface ds;
    ImageView startImage, endImage;
    ArrayList<ImageView> blockImage = new ArrayList<ImageView>();
    TableLayout table;

    // addContentView를 통해 surfaceview를 올리는 메소드
    private void addSurfaceView() {
        ds = new DrawingSurface(this);
        ds.setZOrderOnTop(true);
        ds.getHolder().setFormat(PixelFormat.TRANSPARENT);
        addContentView(ds, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    }

    private void removeSurfaceView() {
        ((ViewManager) ds.getParent()).removeView(ds);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //시작 시 collision, start, end boolean값을 false로 초기화한다.
        collision = start = end = false;
        setContentView(R.layout.hw4b_table);

        //surfaceview를 기존 table로 구성된 레이아웃 위에 올림
        addSurfaceView();

        i =10; //max row
        j =10; //max cloumn

        // 미로만들기
        maze = new int[i][j];

        //미로가 가능한것인지 판별하기 위한 solver 생성
        MazeSolver ms = new MazeSolver(i, j, maze);

        // 미로초기화 - random 하게 생성
        do {
            for (int ni = 0; ni < i; ni++) {
                for (int nj = 0; nj < j; nj++) {
                    if ((ni == 0 && nj == 0) || (ni == i - 1 && nj == j - 1)) {
                        maze[ni][nj] = 0;
                    }
                    else {
                        maze[ni][nj] = ((int) (Math.random() * 10) % 2);
                    }
                }
            }
        } while (!ms.findPath(0, 0));//solver를 통해 path를 찾으면 while문을 빠져나옴

        //가장 아래에 깔린 레이아웃(tablelayout)을 가져옴
        table = (TableLayout) findViewById(R.id.hw4b_table);

        //그림을 중앙에 정렬하기 위해서셀들의 너비를 구함
        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        int cellWidth = display.getWidth() / i;
        int cellHeight = (display.getHeight() - 80) / j;

        //이미지를 가져오고 그 이미지를 구한 너비에 맞게 bitmap 크기를 조절
        block = Bitmap.createScaledBitmap(((BitmapDrawable) getResources().getDrawable(R.drawable.block2)).getBitmap(), cellWidth,
                cellHeight, false);
        point_s = Bitmap.createScaledBitmap(((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), cellWidth,
                cellHeight, false);
        point_e = Bitmap.createScaledBitmap(((BitmapDrawable) getResources().getDrawable(R.drawable.point)).getBitmap(), cellWidth,
                cellHeight, false);
        road = Bitmap.createScaledBitmap(((BitmapDrawable) getResources().getDrawable(R.drawable.road)).getBitmap(), cellWidth,
                cellHeight, false);


        // 얻어온 tablelayout에 생성한 bitmap을 활용한 imageview를 포함하는 row들을 추가함.
        for (int nj = 0; nj < j; nj++) {
            TableRow r = new TableRow(getApplicationContext());

            for (int ni = 0; ni < i; ni++) {
                ImageView v = new ImageView(getApplicationContext());
                v.setLayoutParams(new TableRow.LayoutParams(cellWidth, cellHeight));//Imageview의 크기를 설정

                if ((nj == 0 && ni == 0)) {
                    // 왼쪽 상단을 start_point로,
                    v.setImageBitmap(point_s);
                    startImage = v;
                } else if ((ni == i - 1 && nj == j - 1)) {
                    // 오른쪽 하단을 end_point로.
                    v.setImageBitmap(point_e);
                    endImage = v;
                }
                else {
                    if (maze[ni][nj] == 1) { //배열 안의 값이 1인 경우 벽 이미지를 표시함.
                        v.setImageBitmap(block);
                        blockImage.add(v);
                    }
                    else
                        v.setImageBitmap(road);//아무것도 넣지 않으면 생략되는 경우 발생
                }
                r.addView(v);
            }
            table.addView(r);
        }

    }

    //touch 이벤트와 surfaceview를 사용한 surfaceview 그림 그리기
    public class DrawingSurface extends SurfaceView implements SurfaceHolder.Callback {
        Canvas cacheCanvas;
        Bitmap backBuffer;
        Paint paint;
        Context context;
        SurfaceHolder mHolder;
        boolean notify = true;
        int width, height, clientHeight;
        int lastX, lastY, currX, currY;
        boolean isDeleting;

        public DrawingSurface(Context context) {
            super(context);
            this.context = context;
            init();
        }

        public DrawingSurface(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.context = context;
            init();
        }

        private void init() {
            mHolder = getHolder();
            mHolder.addCallback(this);
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        public void surfaceCreated(SurfaceHolder holder) {
            width = getWidth();
            height = getHeight();
            cacheCanvas = new Canvas();
            backBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            cacheCanvas.setBitmap(backBuffer);
            cacheCanvas.drawColor(Color.TRANSPARENT);

            paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStrokeWidth(8);
            draw();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);

            int action = event.getAction();
            switch (action & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    lastX = (int) event.getX();
                    lastY = (int) event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isDeleting)
                        break;
                    currX = (int) event.getX();
                    currY = (int) event.getY();

                    if (currX >= table.getX() && currX <= table.getX() + table.getWidth() && currY >= table.getY() && currY <= table.getY() + table.getHeight()) {//그리는 영역 미로안에 포함된 경우에만 그
                        cacheCanvas.drawLine(lastX, lastY, currX, currY, paint);
                        lastX = currX;
                        lastY = currY;

                        // start를 지나간 경우(이미지 좌표를 통해 판별) start boolean값을 true로 설정함.
                        startImage.getLocationOnScreen(imageXY);
                        if (imageXY[0] <= currX && imageXY[0] + startImage.getWidth() >= currX && imageXY[1] - getClientTopHeight() <= currY && imageXY[1] - getClientTopHeight() + startImage.getHeight() >= currY) {
                            start = true;
                        }

                        // end 포인트 처리 & 엔드 포인트의 위치 처리
                        endImage.getLocationOnScreen(imageXY);
                        if (imageXY[0] <= currX && imageXY[0] + endImage.getWidth() >= currX && imageXY[1] - getClientTopHeight() <= currY && imageXY[1] - getClientTopHeight() + endImage.getHeight() >= currY) {
                            end = true;
                        }

                        // block 충돌처리. 만약 사용자가 그린 선이 벽의 영역과 겹친다면 collision boolean = true로.
                        for (int i = 0; i < blockImage.size(); i++) {
                            ImageView b = blockImage.get(i);
                            b.getLocationOnScreen(imageXY);
                            if (imageXY[0] <= currX && imageXY[0] + b.getWidth() >= currX && imageXY[1] - getClientTopHeight() <= currY && imageXY[1] - getClientTopHeight() + b.getHeight() >= currY) {
                                collision = true;
                            }
                        }
                        // SurfaceView에 그림
                        draw();
                    }

                    // 미로 영역을 넘어가버린 경우 강제로 upevent를 발생시킴
                    else {
                        MotionEvent up_event = MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0, 0, 0);
                        dispatchTouchEvent(up_event);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if (notify) {//미로찾기에 성공할 경우
                        if (start && end && !collision) {
                            Toast.makeText(getApplicationContext(), "Success!!", Toast.LENGTH_LONG).show();
                            notify = false;
                            isDeleting = true;
                            cleanUp();
                        } else {//미로찾기에 실패할 경우
                            Toast.makeText(getApplicationContext(), "Fail!!", Toast.LENGTH_LONG).show();
                            cleanUp();
                        }
                    }
                    break;
            }
            return true;
        }

        //미로 탈출 경로 그리기에 실패한 경우 호출되는 메소드.
        private void cleanUp() {
            //이동기록 초기
            collision = start = end = false;
            // 이미 올려져있는 surfaceview를 제거하고 새로 만들어서 올린다.
            removeSurfaceView();
            addSurfaceView();
        }

        protected void draw() {
            if (clientHeight == 0) {
                clientHeight = getClientHeight();
                height = clientHeight;
                cacheCanvas.drawColor(Color.TRANSPARENT);
            }
            Canvas canvas = null;
            try {
                canvas = mHolder.lockCanvas(null);
                // back buffer에 그려진 비트맵을 스크린 버퍼에 그린다
                canvas.drawBitmap(backBuffer, 0, 0, paint);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (mHolder != null)
                    mHolder.unlockCanvasAndPost(canvas);
            }
        }

        //타이틀바와 상태바를 제외한 영역의 높이를 구한다
        private int getClientHeight() {
            Rect rect = new Rect();
            Window window = ((Activity) context).getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(rect);
            int statusBarHeight = rect.top;
            int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
            int titleBarHeight = contentViewTop - statusBarHeight;
            return ((Activity) context).getWindowManager().getDefaultDisplay().getHeight() - statusBarHeight - titleBarHeight;
        }

        //타이틀과 상태바의 크기를 리턴하는 method
        private int getClientTopHeight() {
            Rect rect = new Rect();
            Window window = ((Activity) context).getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(rect);

            int statusBarHeight = rect.top;
            int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
            int titleBarHeight = contentViewTop - statusBarHeight;

            return statusBarHeight + titleBarHeight;
        }
    } // class DrawingSurface


    //recusive 한 method 를 통해 이 미로가 풀수 있는 것인지 판단
    public class MazeSolver {
        public int[][] maze;
        public int width, height;
        boolean a, b, c, d;

        //넓이와 높이 미로를 복사
        public MazeSolver(int width, int height, int[][] maze) {
            this.width = width;
            this.height = height;
            this.maze = maze.clone();
        }

        //길을 찾는 method
        public boolean findPath(int row, int col) {
            this.maze[row][col] = 3;

            if (row == width - 1 && col == height - 1) {
                return true;
            } else if (row > width || col > height)
                return false;

            if (row + 1 < width && maze[row + 1][col] == 0) {
                return a = findPath(row + 1, col);
            }
            if (a == false && col + 1 < height && maze[row][col + 1] == 0) {
                return b = findPath(row, col + 1);
            }
            if (a == b == false && row > 1 && maze[row - 1][col] == 0) {
                return c = findPath(row - 1, col);
            }
            if (a == b == c == false && col > 1 && maze[row][col - 1] == 0) {
                return d = findPath(row, col - 1);
            }
            return false;
        }
    }
}// Activity
