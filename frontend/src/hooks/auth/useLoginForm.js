import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../../api/auth';
import useAuthStore from '../../stores/useAuthStore';

// Validation Schema
const loginSchema = z.object({
  email: z.string().email("이메일 형식이 올바르지 않습니다."),
  password: z.string().min(1, "비밀번호를 입력해주세요."),
});

export const useLoginForm = () => {
  const navigate = useNavigate();
  const { setAccessToken } = useAuthStore();
  const [serverError, setServerError] = useState(null);

  const formMethods = useForm({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  });

  const onSubmit = async (data) => {
    try {
      setServerError(null);
      const res = await authApi.login(data.email, data.password);
      setAccessToken(res.accessToken);
      
      // 사용자 정보 가져오기 및 저장
      const profile = await authApi.getMyProfile();
      useAuthStore.getState().setUser(profile);

      navigate('/');
    } catch (error) {
      console.error(error);
      setServerError("이메일 또는 비밀번호가 올바르지 않습니다.");
    }
  };

  return {
    formMethods,
    serverError,
    onSubmit: formMethods.handleSubmit(onSubmit),
    isSubmitting: formMethods.formState.isSubmitting,
  };
};
