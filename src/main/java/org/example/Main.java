package org.example;

public class Main {

    public static void main(String[] args) {
        Console console = new Console();
        console.createConsoleHandle(); //初期化処理
        int i = 0;
        while (i < 1000000) {
            console.clearScreen(); //画面のクリア
            console.print("abcdefghijk"); //文字を出力
            console.swapConsoleHandle(); //コンソールハンドルを入れ替え
        }
        System.out.println("test");
        console.deleteConsoleHandle();     //解放

    }
}
