# nlp.py 나이·취미 등 정보 추출

import os
# import openai
from typing import Dict, Any
import json
from fastapi import HTTPException
from dotenv import load_dotenv 
import google.generativeai as genai

load_dotenv() # 환경 변수 로드
genai.configure(api_key=os.getenv("GOOGLE_API_KEY")) # Gemini API 키 설정

def extract_info_llm(text: str) -> Dict[str, Any]:
    """ LLM 기반 자기소개 텍스트->정보 추출 """

    prompt = (
        "당신은 사용자의 자기소개 내용을 분석하여 프로필 정보를 추출하는 AI 전문가입니다.\n"
        "다음 텍스트에서 이름, 나이, 취미, 성별, 그리고 한 줄 소개(introduction)를 찾아 JSON 형식으로 추출해주세요.\n\n"
        "--- 추출 규칙 ---\n"
        "0.  이름: 문맥에서 2~4글자의 한국어 이름을 추출합니다.\n"
        "   - 예시: '저는 홍길동입니다.'와 같은 내용은 화자의 이름이 \"홍길동\"임을 의미합니다.\n"
        "1. 성별: '남자', '여자' 같은 직접적인 단어뿐만 아니라, **문맥을 통해 논리적으로 추론**해야 합니다.\n"
        "   - 예시 1: '좋은 남자친구나 남편이나 남자를 만나고 싶다' 등의 내용은 화자가 **여자**임을 의미합니다.\n"
        "   - 예시 2: '예쁜 여자친구나 아내나 여사친이 있으면 좋겠다' 등의 내용은 화자가 **남자**임을 의미합니다.\n"
        "   - 성별은 남자는 'M', 여자는 'F'로 표기해주세요.\n"
        "2. 나이: 나잇대나 나이가 숫자로 명확히 언급된 경우, 그것을 위주로 추출합니다. (예: '70살', '65세', '80대 후반')\n"
        "   이때, 나이가 정확히 숫자로 추출되는 경우, 나이에 \'세\'를 붙여 표기합니다.\n"
        "   - 예시: \'70\'이라는 숫자가 추출되는 경우, 나이는 \'70세\'로 표기합니다.\n"  
        "   또한 \'일흔\'과 같이 숫자를 한국어 형태로 언급한 경우, 숫자로 변환해 \'70세\'로 표기합니다."
        "   이때, 나이가 정확히 하나의 숫자로 추출되지 않는 경우, 나이에 \'대\'를 붙여 표기합니다.\n"
        "   - 예시: \'70대 초반\'이나 \'70 언저리\'나 \'70쯤\'과 같이 추출되는 경우, 나이는 \'70대\'로 표기합니다.\n"  
        "3. 취미: 사용자의 활동이나 관심사를 핵심적인 것을 위주로 최대 4개까지 배열 형식으로 추출합니다.\n"
        "   이때, 가능하면 간단한 키워드 위주로 추출해주세요. 예를 들어, \'영화 감상하기\'나 \'영화 감상\'보다는 \'영화\'로 표기하는 것이 더 적절합니다."
        "4. introduction: 추출한 취미를 포함해 \'~에 관심있는 ~한 사람입니다.\', 또는 \'~을/를 좋아하는 ~한 사람입니다.\' 형태의 짧은 문구.\n"
        "   - 성격이나 특성을 문맥에서 추출해 포함.\n"
        "   - 예시: \n"
        "       입력: \"안녕하세요, 놀고 싶은 박하영입니다. 나이는 일흔 하나, 독서와 등산 좋아합니다.\"\n"
        "       출력: {\'name\': \'박하영\', \'age\': \'71세\', \'hobbies\': ['독서','등산'], \'gender\': \'F\', \'introduction\': \"독서와 등산에 관심있는 활발한 사람입니다.\"}\n\n"
        "5. 정보 없음: 어떤 필드의 정보도 찾을 수 없다면, 해당 필드의 값은 `null`로 설정해주세요.\n\n"
        "--- 완벽한 처리 예시 ---\n"
        "- 입력 텍스트: \"안녕하세요, 좋은 남자친구 만나고 싶은 김영희입니다. 나이는 칠십이고, 취미는 뜨개질이에요.\"\n"
        "- 결과 JSON: {\'name\': \'김영희\', \'age\': '70세', \'hobbies\': [\"뜨개질\"], \'gender\': \'F\', \"introduction\": \"뜨개질을 좋아하는 사랑을 원하는 사람입니다.\"}\n\n"
        "- 입력 텍스트: \"씩씩한 박철수입니다. 나이는 70대후반이고, 여자친구 구합니다. 등산 좋아합니다. 영화 보는 것도요.\"\n"
        "- 결과 JSON: {\'name\': \'박철수\', \'age\': '70대', \'hobbies\': [\"등산\", \"영화\"], \'gender\': \'M\', \"introduction\": \"등산과 영화에 관심있는 씩씩한사람입니다.\"}\n\n"
        "--- 분석할 텍스트 ---\n"
        f"\"{text}\""
    )
    
    try:
        # 'gemini-2.5-flash' || 'gemini-pro' 모델을 사용
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

        # LLM이 추출하고 파싱한 직후의 데이터를 터미널에 출력
        print(f"--- NLP 모듈 추출 결과 ---")
        print(extracted_info)
        print(f"--------------------------")

        # 추출된 데이터의 유효성 검사 
        if not isinstance(extracted_info, dict):
            raise ValueError("LLM 응답 형식이 올바르지 않습니다.")
        
        extracted_info = {
            "name": extracted_info.get("name"),
            "age": extracted_info.get("age"),
            "gender": extracted_info.get("gender"),
            "hobbies": extracted_info.get("hobbies", []),
            "introduction": extracted_info.get("introduction")
        }

        # gender 기본값 보정
        if not extracted_info.get("gender"):
            extracted_info["gender"] = "F"

        # hobbies 보정
        if 'hobbies' in extracted_info and not isinstance(extracted_info['hobbies'], list):
            if isinstance(extracted_info['hobbies'], str):
                extracted_info['hobbies'] = [h.strip() for h in extracted_info['hobbies'].split(',') if h.strip()]
            else:
                extracted_info['hobbies'] = []
        else:
            extracted_info['hobbies'] = [h.strip() for h in extracted_info['hobbies'] if h and isinstance(h, str)]

        # introduction 보정
        if 'introduction' in extracted_info and not isinstance(extracted_info['introduction'], str):
            if isinstance(extracted_info['introduction'], list) and len(extracted_info['introduction']) > 0:
                extracted_info['introduction'] = extracted_info['introduction'][0]
            else:
                extracted_info['introduction'] = str(extracted_info['introduction'])


        print("--- NLP 모듈 추출 결과 ---")
        print(extracted_info)
        print("---------------------------")
        return extracted_info

    except json.JSONDecodeError as e:
        print(f"LLM 응답 JSON 파싱 오류: {json_string} - {e}")
        raise HTTPException(status_code=500, detail="AI 정보 추출 결과 형식이 올바르지 않습니다.")
    
    except Exception as e:
        print(f"LLM API 호출 또는 정보 추출 중 오류: {e}")
        raise HTTPException(status_code=500, detail="AI 정보 추출 처리 중 오류가 발생했습니다.")
    
    