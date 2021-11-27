package org.example;

import com.sun.jna.Library;
import com.sun.jna.Structure;
import com.sun.jna.Union;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.Wincon;

public interface CLibrary extends Library {
    @Structure.FieldOrder({"dwSize", "bVisible"})
    public static class CONSOLE_CURSOR_INFO extends Structure {
        public static class ByReference extends CONSOLE_CURSOR_INFO implements Structure.ByValue { }
        public WinDef.DWORD dwSize;
        public WinDef.BOOL bVisible;
        }

    @Structure.FieldOrder({"dwSize", "dwCursorPosition","wAttributes","srWindow" ,"dwMaximumWindowSize"})
    public static class CONSOLE_SCREEN_BUFFER_INFO extends Structure {
        public static class ByReference extends CONSOLE_SCREEN_BUFFER_INFO implements Structure.ByValue { }
        public Wincon.COORD dwSize;
        public Wincon.COORD      dwCursorPosition;
        public WinDef.WORD       wAttributes;
        public Wincon.SMALL_RECT srWindow;
        public Wincon.COORD      dwMaximumWindowSize;
    }

    @Structure.FieldOrder({"UnicodeChar", "AsciiChar"})
    public static class CharUnion extends Union {
        public static class ByValue extends CharUnion implements Union.ByValue {};

        public char  UnicodeChar;
        public WinDef.CHAR  AsciiChar;
    }

    @Structure.FieldOrder({"Char", "Attributes"})
    public static class CHAR_INFO extends Structure {
        public static class ByReference extends CHAR_INFO implements Structure.ByValue { }
        public CharUnion Char;
        public WinDef.WORD Attributes;
    }
}