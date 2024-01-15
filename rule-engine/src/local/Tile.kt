package org.keizar.game.local

class Tile(val symbol: Symbol) {
    var piece: Piece?= null

    enum class Symbol {
        PLAIN, KING, QUEEN, BISHOP, KNIGHT, ROOK, KEIZAR
    }
}
