%% ���ֲ��Ե�30�����н�����նԱ�
% �������ս�������������µ���ʽ��
% data = [0.150000 0.680000;
%     0.070000 0.060000;
%     0.190000 0.050000];

basePath = 'C:/code/OilScheduleOperationPlatform-master/result/Experiment/PF/%s.%s.rf';

%���еĲ���
policies = {'EDF_PS','EDF_TSS','BT'};
%�㷨
algorithm = 'NSGAII';

%��ȡ�ļ��е�ԭʼ����
rawData = cell(1,3);
for i=1:3
    fileName = sprintf(basePath,policies{i},algorithm);
    rawData(i) = {csvread(fileName)};
end

%����Cָ��ֵ
result=zeros(3);
for i=1:3
    for j=1:3
        if i ~= j
            dataSet1 = cell2mat(rawData(i));
            dataSet2 = cell2mat(rawData(j));
            result(i,j) = C(dataSet1,dataSet2);
            fprintf(sprintf('C(%s,%s)=%f',policies{i},policies{j},result(i,j)));
        end
        fprintf('\n');
    end
end

%���������
map = [2, 4, 1; 3, 7, 5; 6, 8, 9];%����ӳ���ϵ������һ�е�һ���ж�Ӧԭ�������еĵ�һ�еڶ��У�...
labels = {'C(EDF_PS,EDF_PS)','C(EDF_PS,EDF_TSS)','C(EDF_PS,BT)';
    'C(EDF_TSS,EDF_PS)','C(EDF_TSS,EDF_TSS)','C(EDF_TSS,BT)';
    'C(BT,EDF_PS)','C(BT,EDF_TSS)','C(BT,BT)'};
resultLabels = cell(3);
for i=1:3
   for j=1:3
       row = ceil(map(i,j)/3);
       col = ceil(mod(map(i,j)-1,3)+1);
       %������
       resultLabels(i,j) = {labels(row,col)};
       data(i,j) = result(row,col);
   end
end

%ȷ����ȷ�����鲻Ҫɾ����
for i=1:3
   for j=1:2
       fprintf('%s=%f\n',cell2mat(resultLabels{i,j}),data(i,j));
   end
end


% ��������ͼ
bar(data(:,1:2));

% ��ȡÿ��ϵ�е�ֵ
AB=data(:,1);
BA=data(:,2);

% ������ݵ���״ͼ
offset_vertical = 0.02;   % ������Ҫ����
offset_horizon = 0.15;  % ������Ҫ����
for i = 1:length(AB)
    if AB(i)>=0
        text(i - offset_horizon,AB(i) + offset_vertical,num2str(AB(i)),'VerticalAlignment','middle','HorizontalAlignment','center');
    else
        text(i - offset_horizon,AB(i) - offset_vertical,num2str(AB(i)),'VerticalAlignment','middle','HorizontalAlignment','center');
    end
end
for i = 1:length(BA)
    if BA(i)>=0
        text(i + offset_horizon,BA(i) + offset_vertical,num2str(BA(i)),'VerticalAlignment','middle','HorizontalAlignment','center','FontName','Times New Roman','FontSize',12);
    else
        text(i + offset_horizon,BA(i) - offset_vertical,num2str(BA(i)),'VerticalAlignment','middle','HorizontalAlignment','center','FontName','Times New Roman','FontSize',12);
    end
end

% ����ͼ����ʾ��ʽ
set(gca,'FontName','Times New Roman','FontSize',12,'LineWidth',1.5);%���������������弰��С
legend({'C(A,B)','C(B,A)'},'FontName','Times New Roman','FontSize',12,'LineWidth',1.5);%����ͼ�������弰��С
xlabel('','FontName','Times New Roman','FontSize',12);%����x���ǩ���弰��С
ylabel('C metrics','Interpreter','latex','FontName','Times New Roman','FontSize',12);%����y���ǩ���弰��С
ylim([0 1]);
set(gca,'TickLabelInterpreter','latex');
set(gca,'xticklabel', {'$$R(EDF_{PS},EDF_{TSS})$$','$$R(EDF_{PS},BT)$$','$$R(EDF_{TSS},BT)$$'});
set(gcf,'Position',[347,162,750,480]);%���û�ͼ��С��λ��