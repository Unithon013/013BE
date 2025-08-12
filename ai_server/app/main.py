from fastapi import FastAPI, UploadFile, File, HTTPException, BackgroundTasks
from fastapi.responses import JSONResponse
import os
import uuid
import json
from typing import Dict

# 로컬 파일 import
from .utils import save_upload_file, remove_file
from .stt import transcribe_video
from .nlp import extract_info_llm

app = FastAPI(
    title="시니어 데이팅 앱 AI 서비스: 불타는 씨니어, 불씨",
    description="자기소개 영상 분석을 통한 사용자 정보 추출 API (백그라운드 처리)",
    version="3.0.0"
)

# 작업 상태와 결과를 저장할 인메모리 딕셔너리
tasks: Dict[str, Dict] = {}

def process_video_in_background(task_id: str, temp_video_path: str):
    """
    실제 분석 작업을 수행하는 백그라운드 함수
    """
    try:
        print(f"[Task: {task_id}] 백그라운드 분석 시작.")
        
        # 1. STT와 NLP 분석을 순차적으로 실행
        transcribed_text = transcribe_video(temp_video_path)
        if not transcribed_text:
            raise ValueError("음성 인식 가능한 텍스트를 찾을 수 없습니다.")
        
        extracted_user_info = extract_info_llm(transcribed_text)
        
        # 2. DTO 형식에 맞게 hobbies 필드를 JSON 문자열로 변환
        if 'hobbies' in extracted_user_info and isinstance(extracted_user_info.get('hobbies'), list):
            extracted_user_info['hobbies'] = json.dumps(extracted_user_info['hobbies'], ensure_ascii=False)
        
        # 3. 작업 상태와 결과 업데이트
        tasks[task_id]['status'] = 'completed'
        tasks[task_id]['result'] = extracted_user_info
        print(f"[Task: {task_id}] 분석 완료.")

    except Exception as e:
        print(f"[Task: {task_id}] 오류 발생: {e}")
        tasks[task_id]['status'] = 'failed'
        tasks[task_id]['error'] = str(e)
    finally:
        # 4. 임시 파일 삭제
        remove_file(temp_video_path)
        print(f"[Task: {task_id}] 임시 파일 삭제 완료.")


@app.post("/process-video", status_code=202, summary="영상 분석 작업 요청 (백그라운드)")
async def request_video_processing(
    background_tasks: BackgroundTasks,
    video: UploadFile = File(..., description="사용자의 자기소개 영상 파일 (mp4, mov 등)")
):
    """
    영상 분석을 요청하고 즉시 작업 ID를 반환합니다.
    """
    temp_video_path = save_upload_file(video)
    task_id = str(uuid.uuid4())
    
    tasks[task_id] = {"status": "processing", "result": None}
    
    # FastAPI의 BackgroundTasks에 실제 분석 함수를 등록합니다.
    background_tasks.add_task(process_video_in_background, task_id, temp_video_path)
    
    return {"task_id": task_id, "message": "영상 분석 작업이 시작되었습니다."}


@app.get("/tasks/{task_id}", summary="작업 상태 및 결과 조회")
async def get_task_status(task_id: str):
    """
    작업 ID를 사용하여 분석 진행 상태와 최종 결과를 확인합니다.
    """
    task = tasks.get(task_id)
    if not task:
        raise HTTPException(status_code=404, detail="작업을 찾을 수 없습니다.")
    
    return JSONResponse(content=task)
