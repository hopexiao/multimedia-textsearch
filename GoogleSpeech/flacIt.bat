"C:\Program Files (x86)\VideoLAN\VLC\vlc.exe" -I dummy "fragment13.mp3" --sout="#transcode{acodec=flac,samplerate=16000}:std{access=file,mux=raw,dst=fragment13.flac}" vlc://quit