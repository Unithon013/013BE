# stt.py 
# Whisper 사용 STT 기능

import whisper
# from .utils import video_to_audio

# Whisper 모델은 앱 시작 시 한 번만 로드되도록 전역 변수/싱글톤 패턴 사용
_model = None

def get_whisper_model(size="small"):
    """Whisper 모델 1회만 로드"""
    global _model

    if _model is None:
        print(f"Whisper 모델({size}) 로드 중...")
        _model = whisper.load_model(size)

    return _model


def transcribe_video(video_path: str) -> str:
    """영상 파일 -> 음성 변환 후 STT 텍스트 반환"""
    try:
        # audio_path = video_to_audio(video_path) # Whisper: ffmpeg 라이브러리로 오디오 스트림 자동 추출: video_path 그대로 사용
        model = get_whisper_model()
        result = model.transcribe(video_path, language="ko") # language="ko" 로 한국어 인식 정확도 향상
        
        transcribed_text = result.get("text", "") # result["text"]
        print(f"STT 결과: {transcribed_text[:100]}...") # 긴 텍스트의 앞부분만 출력
        
        return transcribed_text
    
    except Exception as e:
        print(f"음성 인식(STT) 처리 중 오류 발생: {e}")
        raise