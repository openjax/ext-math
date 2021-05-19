/* Copyright (c) 2014 LibJ
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

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Utility class providing functions related to
 * <a href="https://en.wikipedia.org/wiki/Group_theory">Group Theory</a>, such
 * as <a href="https://en.wikipedia.org/wiki/Combination">combinations</a>,
 * and <a href="https://en.wikipedia.org/wiki/Permutation">permutations</a>.
 */
public final class Groups {
  /**
   * Returns an array of all ordered subset permutations of {@code k} elements
   * from a fixed set of {@code n} elements as {@code int} values from {@code 0}
   * to {@code n}.
   *
   * @param n The number of elements representing the fixed set.
   * @param k The number of elements representing the subsets.
   * @return An array of all ordered subset permutations of {@code k} elements
   *         from a fixed set of {@code n} elements as {@code int} values from
   *         {@code 0} to {@code n}.
   * @throws ArithmeticException If {@code n} is less than {@code k}.
   */
  public static int[][] permute(final int n, final int k) {
    if (n < k)
      throw new ArithmeticException("n (" + n + ") is less than k (" + k + ")");

    final int[] a = new int[n];
    for (int i = 0; i < n; ++i)
      a[i] = i;

    final int size = SafeMath.factorial(n) / SafeMath.factorial(n - k);
    final int[][] permutations = new int[size][];
    enumerate(a, n, k, permutations, 0);
    return permutations;
  }

  /**
   * Recursively enumerates and assigns the permutations for the given values,
   * and returns the number of enumerated permutations.
   *
   * @param a The array.
   * @param n The total number of elements.
   * @param k The subset number of elements in the permutation.
   * @param permutations The resulting array of all unordered subsets.
   * @param index The {@code permutations} element index into which the
   *          permutation is to be assigned.
   * @return The number of enumerated permutations.
   */
  private static int enumerate(final int[] a, final int n, final int k, final int[][] permutations, final int index) {
    if (k == 0) {
      final int[] subArray = new int[a.length - n];
      System.arraycopy(a, n, subArray, 0, subArray.length);
      permutations[index] = subArray;
      return 1;
    }

    int depth = 0;
    for (int i = 0, n1 = n - 1, k1 = k - 1; i < n; ++i) {
      swap(a, i, n1);
      depth += enumerate(a, n1, k1, permutations, index + depth);
      swap(a, i, n1);
    }

    return depth;
  }

  private static void swap(final int[] a, final int i, final int j) {
    final int temp = a[i];
    a[i] = a[j];
    a[j] = temp;
  }

  /**
   * Returns an array of all unordered subset combinations of {@code k} elements
   * from a fixed set of {@code n} elements as {@code int} values from {@code 0}
   * to {@code n}.
   *
   * @param n The number of elements representing the fixed set.
   * @param k The number of elements representing the subsets.
   * @return An array of all unordered subset combinations of {@code k} elements
   *         from a fixed set of {@code n} elements as {@code int} values from
   *         {@code 0} to {@code n}.
   * @throws ArithmeticException If {@code n} is less than {@code k}.
   */
  public static int[][] combine(final int n, final int k) {
    final int[] a = new int[n];
    for (int i = 0; i < n; ++i)
      a[i] = i;

    final int size = SafeMath.factorial(n) / (SafeMath.factorial(k) * SafeMath.factorial(n - k));
    final int[][] permutations = new int[size][];
    combine(a, k, 0, new int[k], permutations, 0);
    return permutations;
  }

  private static int combine(final int[] a, final int k, final int start, final int[] combination, final int[][] combinations, final int index) {
    if (k == 0) {
      combinations[index] = combination.clone();
      return 1;
    }

    int depth = 0;
    for (int i = start, len = a.length - k; i <= len; ++i) {
      combination[combination.length - k] = a[i];
      depth += combine(a, k - 1, i + 1, combination, combinations, index + depth);
    }

    return depth;
  }

  public static void main(final String[] args) {
    final int n = 5;
    final int k = 2;

    final int[][] combinations = permute(n, k);
    Arrays.stream(combinations).forEach(z -> System.out.println(Arrays.toString(z)));
  }

  /**
   * Combines all subsets of elements in the specified 2-dimensional array,
   * where:
   * <ul>
   * <li>{@code n} = the total number of elements in the 2-dimensional
   * array.</li>
   * <li>{@code r} = the length of the first dimension of the 2-dimensional
   * array.</li>
   * </ul>
   * Time Complexity: {@code O(n choose r)}
   *
   * @param <T> The component type of the array.
   * @param a The 2-dimensional array.
   * @return A 2-dimensional array of combination sets for {@code a}.
   * @throws ArrayIndexOutOfBoundsException If {@code a.length == 0}.
   * @throws NullPointerException If {@code a} or any array member of {@code a}
   *           is null.
   */
  @SuppressWarnings("unchecked")
  public static <T>T[][] combine(final T[][] a) {
    int total = a[0].length;
    for (int i = 1; i < a.length; ++i)
      total *= a[i].length;

    final Class<?> componentType = a[0].getClass().getComponentType();
    final T[][] combinations = (T[][])Array.newInstance(componentType, total, 0);

    for (; total > 0; --total) {
      final T[] currentSet = (T[])Array.newInstance(componentType, a.length);
      int position = total;

      // Pick the required element from each list, and add it to the set
      for (int i = 0; i < a.length; ++i) {
        final int length = a[i].length;
        currentSet[i] = a[i][position % length];
        position /= length;
      }

      combinations[total - 1] = currentSet;
    }

    return combinations;
  }

  private Groups() {
  }
}