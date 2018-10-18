/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fasterxml.jackson.core.base;

import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.NumberInput;
import com.fasterxml.jackson.core.json.ReaderBasedJsonParser;
import com.fasterxml.jackson.core.sym.CharsToNameCanonicalizer;

/**
 * will not be use directly
 * just get _parseSlowInt/_parseSlowFloat bytecode and replace to ParserBase
 */
public abstract class DoSParserFixed extends ReaderBasedJsonParser {
  public DoSParserFixed(IOContext ctxt, int features, Reader r,
      ObjectCodec codec, CharsToNameCanonicalizer st,
      char[] inputBuffer, int start, int end, boolean bufferRecyclable) {
    super(ctxt, features, r, codec, st, inputBuffer, start, end, bufferRecyclable);
  }

  private void _parseSlowInt(int expType) throws IOException {
    String numStr = _textBuffer.contentsAsString();
    try {
      int len = _intLength;
      char[] buf = _textBuffer.getTextBuffer();
      int offset = _textBuffer.getTextOffset();
      if (_numberNegative) {
        ++offset;
      }
      // Some long cases still...
      if (NumberInput.inLongRange(buf, offset, len, _numberNegative)) {
        // Probably faster to construct a String, call parse, than to use BigInteger
        _numberLong = Long.parseLong(numStr);
        _numTypesValid = NR_LONG;
      } else {
        // nope, need the heavy guns... (rare case)

        // *** fix DoS attack begin ***
        if (NR_DOUBLE == expType || NR_FLOAT == expType) {
          _numberDouble = Double.parseDouble(numStr);
          _numTypesValid = NR_DOUBLE;
          return;
        }
        if (NR_BIGINT != expType) {
          throw new NumberFormatException("invalid numeric value '" + numStr + "'");
        }
        // *** fix DoS attack end ***

        _numberBigInt = new BigInteger(numStr);
        _numTypesValid = NR_BIGINT;
      }
    } catch (NumberFormatException nex) {
      // Can this ever occur? Due to overflow, maybe?
      _wrapError("Malformed numeric value '" + numStr + "'", nex);
    }
  }
}
