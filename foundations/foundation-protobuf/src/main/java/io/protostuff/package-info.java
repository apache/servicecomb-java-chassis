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

/**
 * <pre>
 * classes in protostuff are "final" or not all suitable for ServiceComb
 *   1.final
 *     io.protostuff.ProtobufOutputEx
 *     io.protostuff.ByteArrayInput
 *   2.not suitable
 *     1) io.protostuff.OutputEx
 *        fieldNumber changed to tag, avoid makeTag every time
 *        not support write packed value
 *        not support ignore default scalar value
 *     2) io.protostuff.SchemaEx
 *        make it simpler
 *     3) codec of map is not compatible to protobuf
 * so we must copy and modified them
 * </pre>
 */

package io.protostuff;