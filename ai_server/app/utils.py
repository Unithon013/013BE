# utils.py

import os
import shutil
import uuid
from fastapi import UploadFile
# from moviepy.editor import VideoFileClip

# 임시 파일 저장 디렉토리
UPLOAD_DIR = "uploads"
os.makedirs(UPLOAD_DIR, exist_ok=True) # 디렉토리가 없으면 생성

def save_upload_file(upload_file: UploadFile) -> str:
    """업로드된 파일을 임시 경로에 저장하고 경로 반환"""
    file_extension = os.path.splitext(upload_file.filename)[1]
    filename = f"{uuid.uuid4()}{file_extension}"
    filepath = os.path.join(UPLOAD_DIR, filename)

    try:
        with open(filepath, "wb") as buffer:
            shutil.copyfileobj(upload_file.file, buffer)
    except Exception as e:
        print(f"파일 저장 중 오류 발생: {e}")
        raise
    finally:
        upload_file.file.close() # 파일 핸들 닫기

    return filepath

# # Whisper: ffmpeg 라이브러리로 오디오 스트림 자동 추출: video_path 그대로 사용
# def video_to_audio(video_path: str) -> str:
#     """영상에서 오디오(WAV) 추출"""
#     audio_path = os.path.splitext(video_path)[0] + ".wav"
#     clip = VideoFileClip(video_path)
#     clip.audio.write_audiofile(audio_path, codec='pcm_s16le')
#     return audio_path


def remove_file(filepath: str):
    """파일 삭제"""
    if os.path.exists(filepath):
        try:
            os.remove(filepath)
            print(f"파일 삭제 완료: {filepath}")
        except Exception as e:
            print(f"파일 삭제 중 오류 발생: {e}")