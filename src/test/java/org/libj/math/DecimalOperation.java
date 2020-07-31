/* Copyright (c) 2020 Seva Safris, LibJ
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * You should have received a copy of The MIT License (MIT) along with this
 * program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package org.libj.math;

import static org.libj.lang.Strings.Align.*;
import static org.libj.math.DecimalTest.*;
import static org.libj.math.FixedPoint.*;

import java.math.BigDecimal;

import org.libj.console.Ansi.Color;
import org.libj.lang.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class DecimalOperation<T,C> {
  static final Logger logger = LoggerFactory.getLogger(DecimalOperation.class);

  private final String label;
  private final Class<?> arg;
  private final String operator;

  DecimalOperation(final String label, final Class<?> arg, final String operator) {
    this.label = label;
    this.arg = arg;
    this.operator = operator;
  }

  abstract C control(BigDecimal bd1, BigDecimal bd2, long[] time);
  abstract T test(long ld1, long ld2, BigDecimal bd1, BigDecimal bd2, byte bits, long defaultValue, long[] time);
  abstract BigDecimal run(BigDecimal bd1, BigDecimal bd2, C expected, T actual, byte bits, long defaultValue, BigDecimal[] errors, boolean[] failures);

  boolean lockScale() {
    return false;
  }

  byte maxValuePower(final byte scaleBits) {
    return valueBits(scaleBits);
  }

  final long randomBounded(final long min, final long max, final short scale, final byte scaleBits) {
    if (min > max)
      throw new IllegalArgumentException();

    final byte valueBits = valueBits(scaleBits);
    long value = min != max ? (long)(random.nextDouble() * (max - min)) + min : min;
    if (value < Decimal.minValue(valueBits))
      value = Decimal.minValue(valueBits);
    else if (value > Decimal.maxValue(valueBits))
      value = Decimal.maxValue(valueBits);

    final long defaultValue = random.nextLong();
    final long result = encode(value, scale, scaleBits, defaultValue);
    if (result == defaultValue) {
      encode(value, scale, scaleBits, defaultValue);
      throw new IllegalStateException();
    }

    return result;
  }

  final long randomEncoded(final byte bits) {
    final long defaultValue = random.nextLong();
    final long value = randomValue(maxValuePower(bits));
    final short scale = randomScale(bits);
    final long result = encode(value, scale, bits, defaultValue);
    if (result == defaultValue) {
      randomScale(bits);
      encode(value, scale, bits, defaultValue);
      throw new IllegalStateException();
    }

    return result;
  }

  short randomScale(final byte bits) {
    if (bits <= 1)
      return 0;

    if (bits == 2)
      return (short)(Math.random() < 0.5 ? -1 : 0);

    final short maxScale = Decimal.maxScale[bits];
    final double scale = random.nextDouble() * maxScale;
    return (short)((Math.random() < 0.5 ? -1 : 1) * scale);
  }

  final void print(final int count, final long[] time, final BigDecimal[] errors, final boolean[] failures) {
    final StringBuilder builder = new StringBuilder();
    boolean outputErrors = false;
    BigDecimal error = BigDecimal.ZERO;
    for (int i = 0; i < errors.length; ++i) {
      outputErrors |= failures[i];
      if (errors[i] != null)
        error = error.add(errors[i]);
    }

    error = error.divide(BigDecimal.valueOf(errors.length));

    if (outputErrors) {
      for (int i = 0; i < errors.length; ++i) {
        if (i > 0)
          builder.append(",\n");

        final Color color = failures[i] ? Color.RED : Color.GREEN;
        if (errors[i] == null)
          builder.append(c(color, "null"));
        else
          builder.append(c(color, "D(\"" + epsilonFormatter.format(errors[i]) + "\")"));
      }

      Strings.replace(builder, "\n", "\n    ");
      builder.insert(0, " | errors=\n  private static final BigDecimal[] epsilon = {\n    ");
      builder.append("\n  };");
    }

    final long timeDecimal = time[0] / numTests;
    final long timeBigDecimal = time[1] / numTests;
    final long timePerf = timeDecimal == 0 ? 0 : ((timeBigDecimal - timeDecimal) * 1000) / timeDecimal;
    String perf = String.valueOf(timePerf);
    perf = perf.substring(0, perf.length() - 1) + "." + perf.substring(perf.length() - 1);
    perf = c(timePerf <= 0 ? Color.RED : Color.GREEN, Strings.pad(timePerf > 0 ? "+" + perf : perf, LEFT, 8) + "%");

    final String f = Strings.pad(label + "(" + arg.getSimpleName() + ")", LEFT, 18);
    final String l = Strings.pad("Decimal=" + timeDecimal, RIGHT, 17);
    final String b = Strings.pad("BigDecimal=" + timeBigDecimal, RIGHT, 20);
    final String c = Strings.pad(String.valueOf(count), RIGHT, 12);
    final String e = Strings.pad(epsilonFormatter.format(error), RIGHT, 6);
    logger.info(f + " | " + l + " | " + b  + " | count=" + c + " | perf=" + perf + " | error=" + e + builder);
  }

  String format1(final long value, final short scale) {
    return Decimal.toString(value, scale);
  }

  String format2(final long value, final short scale) {
    return Decimal.toString(value, scale);
  }

  final String toString(final long v1, final short s1, final long v2, final short s2) {
    return String.format(operator, format1(v1, s1), format2(v2, s2));
  }
}