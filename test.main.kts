import kotlin.math.abs

fun subNibble(a: Int, b: Int) = (a - b).coerceAtLeast(0)

fun getNibble(value: Int, index: Int) = (value ushr (index * 4)) and 0xF

fun pack(a: Int, b: Int) = (a shl 4) or b

fun pack(a: Int, b: Int, c: Int, d: Int) = (a shl 12) or (b shl 8) or (c shl 4) or d

fun sub2(a: Int, b: Int): Int {
    val a1 = getNibble(a, 1) // more significant
    val a2 = getNibble(a, 0) // less significant

    val b1 = getNibble(b, 1)
    val b2 = getNibble(b, 0)

    val c1a = subNibble(a1, b1)
    val c1b = subNibble(b1, a1)
    val c2a = subNibble(a2, b2)
    val c2b = subNibble(b2, a2)

    var sign1: Boolean
    var sign2: Boolean

    var c1 = if (c1a >= c1b) {
        sign1 = false // positive
        c1a
    } else {
        sign1 = true // negative
        c1b
    }
    var c2 = if (c2a >= c2b) {
        sign2 = false // positive
        c2a
    } else {
        sign2 = true // negative
        c2b
    }

    if (c1 == 0 && sign2 /* is negative */) {
        sign1 = true
    }

    // carry the one
    if ((sign1 xor sign2) && (c2 != 0)) {
        c1 = subNibble(c1, 1)
        c2 = subNibble(15, subNibble(c2, 1))
    }

    // END
    val result = pack(c1, c2)
    return if (sign1 /* is negative */) -result else result
}

fun sub4(a: Int, b: Int): Int {
    val a1 = getNibble(a, 3) // more significant
    val a2 = getNibble(a, 2)
    val a3 = getNibble(a, 1)
    val a4 = getNibble(a, 0) // less significant

    val b1 = getNibble(b, 3)
    val b2 = getNibble(b, 2)
    val b3 = getNibble(b, 1)
    val b4 = getNibble(b, 0)

    val c1a = subNibble(a1, b1)
    val c1b = subNibble(b1, a1)
    val c2a = subNibble(a2, b2)
    val c2b = subNibble(b2, a2)
    val c3a = subNibble(a3, b3)
    val c3b = subNibble(b3, a3)
    val c4a = subNibble(a4, b4)
    val c4b = subNibble(b4, a4)

    var sign1: Boolean
    var sign2: Boolean
    var sign3: Boolean
    var sign4: Boolean

    var c1 = if (c1a >= c1b) {
        sign1 = false // positive
        c1a
    } else {
        sign1 = true // negative
        c1b
    }
    var c2 = if (c2a >= c2b) {
        sign2 = false // positive
        c2a
    } else {
        sign2 = true // negative
        c2b
    }
    var c3 = if (c3a >= c3b) {
        sign3 = false // positive
        c3a
    } else {
        sign3 = true // negative
        c3b
    }
    var c4 = if (c4a >= c4b) {
        sign4 = false // positive
        c4a
    } else {
        sign4 = true // negative
        c4b
    }

    if (c3 == 0 && sign4 /* is negative */) {
        sign3 = true
    }

    if (c2 == 0 && sign3 /* is negative */) {
        sign2 = true
    }

    if (c1 == 0 && sign2 /* is negative */) {
        sign1 = true
    }

    if ((sign3 xor sign4) && (c4 != 0)) {
        if (c3 == 0) {
            sign3 = true
        } else {
            c3 = subNibble(c3, 1)
            c4 = subNibble(15, subNibble(c4, 1)) // 16 - c4, but we know c4 is in [1, 15]
        }
    }

    if ((sign2 xor sign3) && (c3 != 0)) {
        if (c2 == 0) {
            sign2 = true
        } else {
            c2 = subNibble(c2, 1)
            c3 = subNibble(15, subNibble(c3, 1))
        }
    }

    if ((sign1 xor sign2) && (c2 != 0)) {
        if (c1 == 0) {
            sign1 = true
        } else {
            c1 = subNibble(c1, 1)
            c2 = subNibble(15, subNibble(c2, 1))
        }
    }

    // END
    val result = pack(c1, c2, c3, c4)
    return result//if (sign1 /* is negative */) -result else result
}

var errors = 0
val max = 0xFFFF

repeat(max) { a ->
    if (errors == 0) {
        if (a % 100 == 0) {
            println(a / 100 * 100)
        }

        repeat(max) { b ->
            val c1 = abs(a - b) // TODO
            val c2 = sub4(a, b)

            if (c1 != c2) {
                System.err.println("${a.toString(16)} - ${b.toString(16)} = ${c1.toString(16)} but I think it's ${c2.toString(16)}")
                errors++
            }
        }
    }
}

println("$errors errors, ${errors.toFloat() / (max * max) * 100}%")