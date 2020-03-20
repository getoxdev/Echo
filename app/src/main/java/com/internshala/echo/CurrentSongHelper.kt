package com.internshala.echo

class CurrentSongHelper {
    var songID: Long? = 0
    var songArtist: String? = null
    var songTitle: String? = null
    var songPath: String? = null
    var currentPosition: Int = 0
    var isPlaying: Boolean = false
    var isLoop: Boolean = false
    var isShuffle: Boolean = false
}