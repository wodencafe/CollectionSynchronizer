/* 
 * BSD 3-Clause License (https://opensource.org/licenses/BSD-3-Clause)
 *
 * Copyright (c) 2018, Christopher Bryan Boyd <wodencafe@gmail.com> All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors 
 *    may be used to endorse or promote products derived from this software 
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package club.wodencafe.decorators;

import java.util.concurrent.CompletableFuture;

/**
 * <h1>RunnableCloseable</h1> 
 * <strong>RunnableCloseable</strong> is a utility interface 
 * that combines {@link java.lang.Runnable} and
 * {@link java.lang.AutoCloseable}, and provides a {@link #runAsync} method.
 *  
 * @author Christopher Bryan Boyd <wdodencafe@gmail.com>
 * @version 0.1
 * @since 2018-01-07
 * @see {@link java.lang.System#identityHashCode}
 *
 */
public interface RunnableCloseable extends AutoCloseable, Runnable
{
	public boolean isClosed();
	
	public CompletableFuture<Void> runAsync();
}
