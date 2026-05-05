import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../../api/auth';

// Validation Schema
const signupSchema = z.object({
  email: z.string().email("이메일 형식이 올바르지 않습니다."),
  username: z.string().min(2, "사용자 이름은 2글자 이상이어야 합니다."),
  password: z.string().min(8, "비밀번호는 8글자 이상이어야 합니다."),
});

export const useSignupForm = () => {
  const navigate = useNavigate();
  const [serverError, setServerError] = useState(null);

  const formMethods = useForm({
    resolver: zodResolver(signupSchema),
    defaultValues: {
      email: '',
      username: '',
      password: '',
    },
  });

  const onSubmit = async (data) => {
    try {
      setServerError(null);
      await authApi.signup(data);
      alert("회원가입이 완료되었습니다. 로그인해주세요.");
      navigate('/login');
    } catch (error) {
      console.error(error);
      setServerError("회원가입 중 오류가 발생했습니다. (이미 존재하는 이메일일 수 있습니다)");
    }
  };

  return {
    formMethods,
    serverError,
    onSubmit: formMethods.handleSubmit(onSubmit),
    isSubmitting: formMethods.formState.isSubmitting,
  };
};
