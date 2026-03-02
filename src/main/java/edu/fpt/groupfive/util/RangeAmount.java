package edu.fpt.groupfive.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class RangeAmount {

    public List<BigDecimal> applyRangeAMount(String amountRange){

        // khởi tạo list
        List<BigDecimal> list = new ArrayList<>();

        if(amountRange == null || amountRange.isBlank()) return null;

        if(amountRange.endsWith("+")){
            amountRange = amountRange.substring(0, amountRange.length()-1);

            list.add(new BigDecimal(amountRange));
        }else{

            // tách chuỗi
            String[] s =  amountRange.split("-");

            // add theo thứ tự min -> max
            list.add(new BigDecimal(s[0]));
            list.add(new BigDecimal(s[1]));
        }

        return list;
    }
}
