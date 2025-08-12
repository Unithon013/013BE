import whisper

# Whisper 모델은 앱 시작 시 한 번만 로드되도록 전역 변수로 관리
_model = None

def load_whisper_model(size="small"):
    """
    [신규] Whisper 모델을 전역 변수에 로드하는 함수. 서버 시작 시 호출됩니다.
    """
    global _model
    if _model is None:
        print(f"Whisper 모델({size})을 미리 로드합니다...")
        _model = whisper.load_model(size)
        print("Whisper 모델 로드 완료.")

def get_whisper_model():
    """
    [수정] 이미 로드된 모델을 반환하는 함수.
    """
    global _model
    if _model is None:
        # 이 경우는 서버 시작 시 로딩에 실패한 예외적인 상황
        raise RuntimeError("Whisper 모델이 로드되지 않았습니다. 서버 시작 로그를 확인하세요.")
    return _model

def transcribe_video(video_path: str) -> str:
    """영상 파일 -> STT 텍스트 반환"""
    try:
        model = get_whisper_model() # 이미 로드된 모델을 가져옴
        result = model.transcribe(video_path, language="ko")
        
        transcribed_text = result.get("text", "")
        print(f"STT 결과: {transcribed_text[:100]}...")
        
        return transcribed_text
    
    except Exception as e:
        print(f"음성 인식(STT) 처리 중 오류 발생: {e}")
        raise
