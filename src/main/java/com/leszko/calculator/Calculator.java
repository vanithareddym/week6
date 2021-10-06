package com.leszko.calculator;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * The type Calculator.
 */
@Service
public class Calculator {
   /**
    * The constant umlNUMBER1.
    */
   final static int umlNUMBER1 = 3;

   /**
    * Sum int.
    *
    * @param a the a
    * @param b the b
    * @return the int
    */
   @Cacheable("sum")
   public int sum(int a, int b) {
      return a + b;
   }
}
