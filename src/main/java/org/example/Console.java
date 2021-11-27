package org.example;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.Wincon;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.win32.W32APIOptions;

import java.util.ArrayList;
import java.util.List;

import static com.sun.jna.platform.win32.WinNT.GENERIC_READ;
import static com.sun.jna.platform.win32.WinNT.GENERIC_WRITE;


public class Console {

    static WinDef.SHORT BLACK = new WinDef.SHORT(0);
    static WinDef.SHORT WHITE =  new WinDef.SHORT(15 << 4);
    static int COLORS = 16;

    static WinNT.HANDLE consoleHandle1;
    static WinNT.HANDLE consoleHandle2;
    static boolean isSwap;

    static CLibrary.CONSOLE_CURSOR_INFO.ByReference cursorInfo = new CLibrary.CONSOLE_CURSOR_INFO.ByReference();
    static Wincon.CONSOLE_SCREEN_BUFFER_INFO screenInfo = new Wincon.CONSOLE_SCREEN_BUFFER_INFO();
    static WinDef.DWORD CONSOLE_TEXTMODE_BUFFER = new WinDef.DWORD(1);

    static List<CLibrary.CHAR_INFO> list = new ArrayList<>();
    static CLibrary.CHAR_INFO[] buffer;


    public interface MyKernel32 extends Kernel32 {
        MyKernel32 INSTANCE = Native.load("kernel32", MyKernel32.class, W32APIOptions.DEFAULT_OPTIONS);

        // Mapped functions go here
        WinNT.HANDLE CreateConsoleScreenBuffer(
                DWORD               dwDesiredAccess,
                DWORD               dwShareMode,
                SECURITY_ATTRIBUTES lpSecurityAttributes,
                DWORD               dwFlags,
                LPVOID              lpScreenBufferData
        );

        WinDef.BOOL SetConsoleCursorInfo(
                HANDLE              hConsoleOutput,
                CLibrary.CONSOLE_CURSOR_INFO  lpConsoleCursorInfo
        );

        WinDef.BOOL FillConsoleOutputAttribute(
                HANDLE  hConsoleOutput,
                WORD    wAttribute,
                DWORD   nLength,
                COORD   dwWriteCoord,
                DWORDByReference lpNumberOfAttrsWritten
        );

        WinDef.BOOL FillConsoleOutputCharacter(
                HANDLE  hConsoleOutput,
                char   cCharacter,
                DWORD   nLength,
                COORD   dwWriteCoord,
                DWORDByReference lpNumberOfCharsWritten
        );

        WinDef.BOOL SetConsoleActiveScreenBuffer(
                HANDLE hConsoleOutput
        );

        WinDef.BOOL WriteConsoleOutputA(
                HANDLE      hConsoleOutput,
                CLibrary.CHAR_INFO[]   lpBuffer,
                COORD       dwBufferSize,
                COORD       dwBufferCoord,
                SMALL_RECT lpWriteRegion
        );
    }

    static void createConsoleHandle() {
        //コンソールハンドルを作る
        consoleHandle1 = MyKernel32.INSTANCE.CreateConsoleScreenBuffer(
                new WinDef.DWORD(GENERIC_READ | GENERIC_WRITE),
                new WinDef.DWORD(0), null, CONSOLE_TEXTMODE_BUFFER, null);
        consoleHandle2 = MyKernel32.INSTANCE.CreateConsoleScreenBuffer(
                new WinDef.DWORD(GENERIC_READ | GENERIC_WRITE),
                new WinDef.DWORD(0), null, CONSOLE_TEXTMODE_BUFFER, null);

        cursorInfo.dwSize = new WinDef.DWORD(1);
        cursorInfo.bVisible = new WinDef.BOOL(false);
        MyKernel32.INSTANCE.SetConsoleCursorInfo(consoleHandle1, cursorInfo);
        MyKernel32.INSTANCE.SetConsoleCursorInfo(consoleHandle2, cursorInfo);

        MyKernel32.INSTANCE.GetConsoleScreenBufferInfo(getHandle(), screenInfo);

        isSwap = false;         //取り敢えずfalseで初期化

        for(int i = 0; i < (int) screenInfo.dwSize.Y * screenInfo.dwSize.X; i++){
            list.add(new CLibrary.CHAR_INFO());
        }

        buffer = (CLibrary.CHAR_INFO[]) list.get(0).toArray(list.size());

        //バッファーを初期化
        for (int y = 0; y < screenInfo.dwSize.Y;++y) {
            for (int x = 0; x < screenInfo.dwSize.X;++x) {
                buffer[y * (int)screenInfo.dwSize.X + x].Attributes = new WinDef.WORD(0 + 15 << 4);
                buffer[y * (int)screenInfo.dwSize.X + x].Char.UnicodeChar = ' ';
            }
        }
    }

    void clearScreen() {
        WinDef.DWORD   dwNumberOfCharsWritten = new WinDef.DWORD(0); // 書き込まれたセル数
        WinDef.DWORDByReference dwNumberOfCharsWritten_ByRef = new WinDef.DWORDByReference(dwNumberOfCharsWritten);
        Wincon.COORD   coord = new Wincon.COORD();       //書き込みを開始する位置 x:0y:0に設定
        coord.X = 0; coord.Y = 0;
        MyKernel32.INSTANCE.GetConsoleScreenBufferInfo(getHandle(), screenInfo);
        // バッファ内の指定した座標から指定した数の文字セル分だけ、前景色と背景色を設定
        MyKernel32.INSTANCE.FillConsoleOutputAttribute(getHandle(),
                new WinDef.WORD(0 + 15 << 4),
                new WinDef.DWORD(screenInfo.dwSize.X * screenInfo.dwSize.Y),
                coord,
                dwNumberOfCharsWritten_ByRef
        );
        // バッファ内の指定した座標から、指定した文字を指定した数だけ書き込む
        MyKernel32.INSTANCE.FillConsoleOutputCharacter(
                getHandle(),
                ' ',
                new WinDef.DWORD(screenInfo.dwSize.X * screenInfo.dwSize.Y),
                coord,
                dwNumberOfCharsWritten_ByRef
        );
    }

    static void swapConsoleHandle() {
        MyKernel32.INSTANCE.SetConsoleActiveScreenBuffer(getHandle());
        isSwap = !isSwap;
    }

    static void deleteConsoleHandle() {
        MyKernel32.INSTANCE.CloseHandle(consoleHandle2);
        MyKernel32.INSTANCE.CloseHandle(consoleHandle1);
    }

    static WinNT.HANDLE getHandle() {

        return isSwap ? consoleHandle1 : consoleHandle2;
    }

    static void print(String str) {

        Wincon.COORD coord = new Wincon.COORD();       //書き込みを開始する位置 x:0y:0に設定
        coord.X = 0; coord.Y = 0;
        Wincon.COORD size = new Wincon.COORD();
        coord.X = screenInfo.dwSize.X; coord.Y = screenInfo.dwSize.Y;                           //サイズ
        Wincon.SMALL_RECT rect = new Wincon.SMALL_RECT();
        rect.Top = coord.Y; rect.Left = coord.X; rect.Right = screenInfo.dwSize.X; rect.Bottom = screenInfo.dwSize.Y;//書き込む箇所を矩形で指定
        int length = str.length();                                                           //文字の長さ
        for (int x = 0; x < screenInfo.dwSize.X;++x) {
            if (x < str.length()) {
                buffer[x].Char.UnicodeChar = str.charAt(x);
                buffer[x].Attributes = new WinDef.WORD( 0 + 15 << 4);
            }
        }
        MyKernel32.INSTANCE.WriteConsoleOutputA(getHandle(), buffer,size, coord, rect);

    }
}
