/* MPFR internal header related to thread-local variables.

Copyright 2005, 2006, 2007, 2008 Free Software Foundation, Inc.
Contributed by the Arenaire and Cacao projects, INRIA.

This file is part of the MPFR Library.

The MPFR Library is free software; you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation; either version 2.1 of the License, or (at your
option) any later version.

The MPFR Library is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
License for more details.

You should have received a copy of the GNU Lesser General Public License
along with the MPFR Library; see the file COPYING.LIB.  If not, write to
the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston,
MA 02110-1301, USA. */

#ifndef __MPFR_THREAD_H__
#define __MPFR_THREAD_H__

/* Note: Let's define MPFR_THREAD_ATTR even after a #error to make the
   error message more visible (e.g. gcc doesn't immediately stop after
   the #error line and outputs many error messages if MPFR_THREAD_ATTR
   is not defined). But some compilers will just output a message and
   may build MPFR "successfully" (without thread support). */
#ifndef MPFR_THREAD_ATTR
# ifdef MPFR_USE_THREAD_SAFE
#  if defined(_MSC_VER)
#   if defined(_WINDLL)
#    error "Can't build MPFR DLL as thread safe."
#    define MPFR_THREAD_ATTR
#   else
#    define MPFR_THREAD_ATTR __declspec( thread )
#   endif
#  else
#   define MPFR_THREAD_ATTR __thread
#  endif
# else
#  define MPFR_THREAD_ATTR
# endif
#endif

#endif
