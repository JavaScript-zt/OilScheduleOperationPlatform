%�״�ͼ
data = csvread('100-100-NSGAII/Experiment/PF/oilschedule.pf');
varNames = { 'energyCost', 'pipeMixingCost', 'tankMixingCost', 'numberOfChange', 'numberOfTankUsed'};

figure;
glyphplot(data,'glyph','star','varLabels',{ 'energyCost', 'pipeMixingCost', 'tankMixingCost', 'numberOfChange', 'numberOfTankUsed'});
set(gcf,'Position',[600,162,900,850]);%���û�ͼ��С��λ��
box off
axis off

ND = normlization(data, 1);
% �������
k = 9;
colors =  lines(k);
label = init_methods(ND, k, 2);

figure;
[data,ind] = sortrows([data,label],6);
h=glyphplot(data(:,1:5),'glyph','star','varLabels',{ 'energyCost', 'pipeMixingCost', 'tankMixingCost', 'numberOfChange', 'numberOfTankUsed'},...
    'ObsLabels',num2str(ind));
for i=1:k
    hf = h(data(:,6)==i,1);
    ha = h(data(:,6)==i,2);
    for j=1:length(hf)
        hf(j).Color=colors(i,:);
        ha(j).Color=colors(i,:);
    end
end
set(gcf,'Position',[600,162,900,850]);%���û�ͼ��С��λ��
box off
axis off

%% ����Ԥ����
    % ���룺�ޱ�ǩ���ݣ���������ѡ�񷽷�
    % ����������ǩ
function label=init_methods(data, K, choose)
    if choose==1
        %�����ʼ�������ѡK����Ϊ�������ģ�����ŷ�Ͼ�����������㵽����࣬�����ݼ���ΪK�࣬���ÿ�����������ǩ
        [X_num, ~]=size(data);
        rand_array=randperm(X_num);    %����1~X_num֮���������������   
        para_miu=data(rand_array(1:K), :);  %�������ȡǰK��������X������ȡ��K����Ϊ��ʼ��������
        %ŷ�Ͼ��룬���㣨X-para_miu��^2=X^2+para_miu^2-2*X*para_miu'�������СΪX_num*K
        distant=repmat(sum(data.*data,2),1,K)+repmat(sum(para_miu.*para_miu,2)',X_num,1)-2*data*para_miu';
        %����distantÿ����Сֵ���ڵ��±�
        [~,label]=min(distant,[],2);
    elseif choose==2
        %��kmeans���г�ʼ�����࣬�����ݼ���ΪK�࣬���ÿ�����������ǩ
        label=kmeans(data, K);
    elseif choose==3
        %��FCM�㷨���г�ʼ��
        options=[NaN, NaN, NaN, 0];
        [~, responsivity]=fcm(data, K, options);   %��FCM�㷨��������Ⱦ���
        [~, label]=max(responsivity', [], 2);
    end
end

%% ���ݹ�һ��
function data = normlization(data, choose)
    if choose==0
        % ����һ��
        data = data;
    elseif choose==1
        % Z-score��һ��
        data = bsxfun(@minus, data, mean(data));
        data = bsxfun(@rdivide, data, std(data));
    elseif choose==2
        % ���-��С��һ������
        [data_num,~]=size(data);
        data=(data-ones(data_num,1)*min(data))./(ones(data_num,1)*(max(data)-min(data)));
    end
end