# main.py
# API 라우팅 (FastAPI)

from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.responses import JSONResponse
import os

from .utils import save_upload_file, remove_file
from .stt import transcribe_video
from .nlp import extract_info_llm

app = FastAPI(
    title="시니어 데이팅 앱 AI 서비스: 불타는 씨니어, 불씨",
    description="자기소개 영상 분석을 통한 사용자 정보 추출 API",
    version="1.0.0"
)

@app.post("/analyze-video", summary="자기소개 영상 분석 및 사용자 정보 추출")
async def analyze_video_endpoint(
    video_file: UploadFile = File(..., description="사용자의 자기소개 영상 파일 (mp4, mov 등)")
):
    temp_video_path = None
    try:
        # 1. 업로드된 영상 파일을 임시로 저장
        temp_video_path = save_upload_file(video_file)
        print(f"영상 파일 임시 저장 완료: {temp_video_path}")

        # 2. STT 서비스로 영상에서 한국어 텍스트 추출
        transcribed_text = transcribe_video(temp_video_path)
        if not transcribed_text:
            raise HTTPException(status_code=400, detail="음성 인식 가능한 텍스트를 찾을 수 없습니다. 영상을 다시 확인해주세요.")
        
        # 3. NLP 서비스로 텍스트에서 사용자 정보 추출 (LLM 활용)
        extracted_user_info = extract_info_llm(transcribed_text)
        
        return JSONResponse(content={
            "status": "success",
            "extracted_info": extracted_user_info,
            "raw_stt_text_data": transcribed_text # 디버깅 또는 추가 분석을 위해 원본 텍스트도 반환
        })

    except HTTPException as e:
        # FastAPI의 HTTPException은 그대로 다시 발생
        raise e
    except Exception as e:
        # 예상치 못한 다른 오류 처리
        print(f"API 처리 중 치명적인 오류 발생: {e}")
        raise HTTPException(status_code=500, detail=f"서버 내부 오류가 발생했습니다: {e}")
    finally:
        # 4. 임시 파일 삭제 (오류 발생 여부와 관계없이 실행)
        if temp_video_path:
            remove_file(temp_video_path)