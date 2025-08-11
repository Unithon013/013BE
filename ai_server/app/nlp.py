# nlp.py 나이·취미 등 정보 추출

import os
# import openai
from typing import Dict, Any
import json
from fastapi import HTTPException
from dotenv import load_dotenv 
import google.generativeai as genai

load_dotenv() # 환경 변수 로드
# openai.api_key = os.getenv("OPENAI_API_KEY")
genai.configure(api_key=os.getenv("GOOGLE_API_KEY"))

def extract_info_llm(text: str) -> Dict[str, Any]:
    """ LLM 기반 자기소개 텍스트->정보 추출 """

    prompt = (
        "다음은 사용자의 자기소개 텍스트입니다. "
        "여기서 이름, 나이, 그리고 취미(최대 4개, 핵심적인 것 위주)를 찾아 JSON 형식으로 추출해주세요. "
        "만약 정보가 없거나 불분명하면 해당 필드를 null로 표시해주세요. "
        "취미는 배열 형식으로 표현해주세요. "
        "예시: {'name': '김철수', 'age': '72', 'hobbies': ['바둑', '독서', '요리']}\n\n"
        "텍스트:\n" + text
    )
    
    try:
        # 'gemini-pro' 모델을 사용
        model = genai.GenerativeModel("gemini-2.5-flash")
        
        # API 호출 및 응답
        response = model.generate_content(
            prompt,
            generation_config=genai.GenerationConfig(
                response_mime_type="application/json"
            )
        )
        
        # Gemini 응답에서 텍스트 콘텐츠 추출
        json_string = response.text
        extracted_info = json.loads(json_string)

        # 추출된 데이터의 유효성 검사 (선택 사항이지만 견고성을 높임)
        if not isinstance(extracted_info, dict):
            raise ValueError("LLM 응답 형식이 올바르지 않습니다.")
        
        # 'hobbies' 필드가 리스트가 아니면 변환
        if 'hobbies' in extracted_info and not isinstance(extracted_info['hobbies'], list):
            if isinstance(extracted_info['hobbies'], str):
                extracted_info['hobbies'] = [h.strip() for h in extracted_info['hobbies'].split(',') if h.strip()]
            else:
                extracted_info['hobbies'] = [] # 알 수 없는 형식은 빈 리스트로 처리

        return extracted_info

    except json.JSONDecodeError as e:
        print(f"LLM 응답 JSON 파싱 오류: {json_string} - {e}")
        raise HTTPException(status_code=500, detail="AI 정보 추출 결과 형식이 올바르지 않습니다.")
    
    except Exception as e:
        print(f"LLM API 호출 또는 정보 추출 중 오류: {e}")
        raise HTTPException(status_code=500, detail="AI 정보 추출 처리 중 오류가 발생했습니다.")
    
    