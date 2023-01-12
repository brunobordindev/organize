package com.example.apporganize.helper;

import java.text.SimpleDateFormat;

public class DateUtil {

    public static String dataAtual(){

        long data = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String dataString = simpleDateFormat.format(data);
        return dataString;
    }

    public static String anoMesDataEscolhida(String data){
        //Ex.   23/03/2022
        String[] retornoData = data.split("/");
        String dia = retornoData[0]; // dia - 23
        String mes = retornoData[1]; // mes - 03
        String ano = retornoData[2]; //ano - 2022

        String mesAno = mes + ano ; // mesAno - 032022

        return mesAno;
    }
}
