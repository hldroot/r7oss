/* Copy memory block and return pointer to beginning of destination block
   For Intel 80x86, x>=6.
   This file is part of the GNU C Library.
   Copyright (C) 1999, 2000, 2003, 2004, 2008 Free Software Foundation, Inc.
   Contributed by Ulrich Drepper <drepper@cygnus.com>, 1999.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, write to the Free
   Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
   02111-1307 USA.  */

#include <sysdep.h>
#include "asm-syntax.h"
#include "bp-sym.h"
#include "bp-asm.h"

#define PARMS	LINKAGE		/* no space for saved regs */
#define RTN	PARMS
#define DEST	RTN+RTN_SIZE
#define SRC	DEST+PTR_SIZE
#define LEN	SRC+PTR_SIZE

	.text
#if defined PIC && !defined NOT_IN_libc
ENTRY (__memcpy_chk)
	movl	12(%esp), %eax
	cmpl	%eax, 16(%esp)
	jb	HIDDEN_JUMPTARGET (__chk_fail)
END (__memcpy_chk)
#endif
ENTRY (BP_SYM (memcpy))
	ENTER

	movl	%edi, %eax
	movl	DEST(%esp), %edi
	movl	%esi, %edx
	movl	SRC(%esp), %esi

	movl	%edi, %ecx
	xorl	%esi, %ecx
	andl	$3, %ecx
	movl	LEN(%esp), %ecx
	cld
	jne	.Lunaligned

	cmpl	$3, %ecx
	jbe	.Lunaligned

	testl	$3, %esi
	je	1f
	movsb
	decl	%ecx
	testl	$3, %esi
	je	1f
	movsb
	decl	%ecx
	testl	$3, %esi
	je	1f
	movsb
	decl	%ecx
1:	pushl	%eax
	movl	%ecx, %eax
	shrl	$2, %ecx
	andl	$3, %eax
	rep
	movsl
	movl	%eax, %ecx
	rep
	movsb
	popl	%eax

.Lend:	movl	%eax, %edi
	movl	%edx, %esi
	movl	DEST(%esp), %eax
	RETURN_BOUNDED_POINTER (DEST(%esp))

	LEAVE
	RET_PTR

	/* When we come here the pointers do not have the same
	   alignment or the length is too short.  No need to optimize for
	   aligned memory accesses. */
.Lunaligned:
	shrl	$1, %ecx
	jnc	1f
	movsb
1:	shrl	$1, %ecx
	jnc	2f
	movsw
2:	rep
	movsl
	jmp	.Lend
END (BP_SYM (memcpy))
libc_hidden_builtin_def (memcpy)
